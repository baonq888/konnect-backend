package com.konnectnet.core.post.service.impl;

import com.konnectnet.core.post.dto.request.PostRequest;
import com.konnectnet.core.post.entity.Photo;
import com.konnectnet.core.post.entity.Post;
import com.konnectnet.core.post.enums.Visibility;
import com.konnectnet.core.post.exception.PostException;
import com.konnectnet.core.post.repository.PostRepository;
import com.konnectnet.core.post.service.PostService;
import com.konnectnet.core.search.LuceneSearchService;
import com.konnectnet.core.search.document.DocumentInfo;
import com.konnectnet.core.search.document.SearchResult;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final LuceneSearchService luceneSearchService;

    @Override
    @Transactional
    public Post createPost(PostRequest request) {
        try {
            Post post = new Post(
                request.getContent(), Visibility.valueOf(request.getVisibility())
            );

            List<Photo> photoList = new ArrayList<>();
            for (String url : request.getPhotoUrls()) {
                photoList.add(new Photo(url));
            }
            post.setPhotos(photoList);

            Post savedPost = postRepository.save(post);

            // Index Post document to Lucence
            DocumentInfo documentInfo = new DocumentInfo(
                    savedPost.getId().toString(),
                    savedPost.getContent(),
                    savedPost.getUser().getName()
            );

            return savedPost;
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage(), e);
            throw new PostException(e.getMessage(), e);

        } catch (Exception e) {
            log.error("Error occurred while creating post: {}", e.getMessage(), e);
            throw new PostException("Failed to create post", e);
        }
    }

    @Override
    public Post getPostById(String postId) {
        try {
            UUID id = UUID.fromString(postId);
            return postRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Post not found with ID: " + postId));
        } catch (EntityNotFoundException e) {
            log.warn("Post not found with ID: {}", postId);
            throw new PostException("Post not found with ID: " + postId, e);
        } catch (Exception e) {
            log.error("Error while fetching post {}: {}", postId, e.getMessage(), e);
            throw new PostException("Failed to retrieve post", e);
        }
    }

    @Override
    public Page<Post> searchPosts(String searchTerm, Pageable pageable) throws IOException {
        // Perform Lucene search
        List<SearchResult> searchResults = luceneSearchService.search(searchTerm, pageable.getPageNumber(), pageable.getPageSize());

        // Convert SearchResults to Post
        List<Post> posts = new ArrayList<>();
        for (SearchResult result : searchResults) {
            Post post = postRepository.findById(result.getPostId())
                    .orElseThrow(() -> new PostException("Post not found for search result"));
            posts.add(post);
        }

        return new PageImpl<>(posts, pageable, searchResults.size());
    }

    @Override
    @Transactional
    public Post updatePost(String postId, PostRequest request) {
        try {
            UUID id = UUID.fromString(postId);
            Post post = postRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Post not found with ID: " + postId));

            if (request.getContent() != null && !request.getContent().isEmpty()) {
                post.setContent(request.getContent());
            }

            if (request.getVisibility() != null) {
                post.setVisibility(Visibility.valueOf(request.getVisibility()));
            }

            // Get the current list of photos
            List<Photo> existingPhotos = post.getPhotos();
            List<String> updatedUrls = request.getPhotoUrls();

            // Remove photos that are no longer part of the updated list
            existingPhotos.removeIf(photo -> !updatedUrls.contains(photo.getUrl()));

            // Add new photos that are not already in the existing list
            for (String url : updatedUrls) {
                if (existingPhotos.stream().noneMatch(photo -> photo.getUrl().equals(url))) {
                    existingPhotos.add(new Photo(url));
                }
            }

            post.setPhotos(existingPhotos);

            return postRepository.save(post);
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage(), e);
            throw new PostException(e.getMessage(), e);

        } catch (Exception e) {
            log.error("Error occurred while updating post: {}", e.getMessage(), e);
            throw new PostException("Failed to update post", e);
        }
    }

    @Override
    @Transactional
    public void deletePost(String postId) {
        try {
            // Fetch the post by ID
            UUID id = UUID.fromString(postId);
            Post post = postRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Post not found with ID: " + postId));

            postRepository.delete(post);

            log.info("Post with ID {} deleted successfully", postId);
        } catch (EntityNotFoundException e) {
            log.warn("Post not found with ID: {}", postId);
            throw new PostException("Post not found with ID: " + postId, e);
        } catch (Exception e) {
            log.error("Error occurred while deleting post {}: {}", postId, e.getMessage(), e);
            throw new PostException("Failed to delete post", e);
        }
    }
}
