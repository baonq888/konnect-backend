package com.konnectnet.core.post.service.impl;

import com.konnectnet.core.auth.entity.AppUser;
import com.konnectnet.core.auth.repository.UserRepository;
import com.konnectnet.core.post.dto.request.PostRequest;
import com.konnectnet.core.post.entity.Comment;
import com.konnectnet.core.post.entity.Photo;
import com.konnectnet.core.post.entity.Post;
import com.konnectnet.core.post.enums.Visibility;
import com.konnectnet.core.post.exception.PostException;
import com.konnectnet.core.post.repository.CommentRepository;
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
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
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
        } catch (Exception e) {
            log.error("Error while fetching post {}: {}", postId, e.getMessage(), e);
            throw new PostException("Failed to retrieve post", e);
        }
    }

    @Override
    public Page<Post> searchPosts(String searchTerm, Pageable pageable) throws IOException {
        try {
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
        } catch (Exception e) {
            log.error("Error while searching posts {}: {}", searchTerm, e.getMessage(), e);
            throw new PostException("Failed to search posts", e);
        }
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
        } catch (Exception e) {
            log.error("Error occurred while deleting post {}: {}", postId, e.getMessage(), e);
            throw new PostException("Failed to delete post", e);
        }
    }

    @Override
    public void likePost(String postId, String userId) {
        try {
            Post post = postRepository.findById(UUID.fromString(postId))
                    .orElseThrow(() -> new EntityNotFoundException("Post not found"));

            AppUser user = userRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            if (!post.getLikedUsers().contains(user)) {
                post.getLikedUsers().add(user);
                postRepository.save(post);
            }
        } catch (Exception e) {
            log.error("Error occurred while liking post {}: {}", postId, e.getMessage(), e);
            throw new PostException("Failed to like post", e);
        }
    }

    @Override
    public void unlikePost(String postId, String userId) {
        try {
            Post post = postRepository.findById(UUID.fromString(postId))
                    .orElseThrow(() -> new RuntimeException("Post not found"));

            AppUser user = userRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (post.getLikedUsers().contains(user)) {
                post.getLikedUsers().remove(user);
                postRepository.save(post);
            }
        } catch (Exception e) {
            log.error("Error occurred while unliking post {}: {}", postId, e.getMessage(), e);
            throw new PostException("Failed to unlike post", e);
        }

    }

    @Override
    public Post sharePost(String postId, String userId, String userContent) {
        try {
            Post originalPost = postRepository.findById(UUID.fromString(postId))
                    .orElseThrow(() -> new RuntimeException("Post not found"));

            AppUser user = userRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Post sharedPost = new Post();
            sharedPost.setContent(userContent);
            sharedPost.setUser(user);
            sharedPost.setOriginalPost(originalPost);
            sharedPost.setVisibility(originalPost.getVisibility());

            return postRepository.save(sharedPost);
        } catch (Exception e) {
            log.error("Error occurred while sharing post {}: {}", postId, e.getMessage(), e);
            throw new PostException("Failed to share post", e);
        }

    }

    @Override
    public void unsharePost(String sharedPostId) {
        try {
            Post sharedPost = postRepository.findById(UUID.fromString(sharedPostId))
                    .orElseThrow(() -> new RuntimeException("Shared post not found"));

            postRepository.delete(sharedPost);
        }  catch (Exception e) {
            log.error("Error occurred while unsharing post {}: {}", sharedPostId, e.getMessage(), e);
            throw new PostException("Failed to unshare post", e);
        }

    }

    @Override
    public Comment commentOnPost(String postId, String userId, String text) {
        try {
            Post post = postRepository.findById(UUID.fromString(postId))
                    .orElseThrow(() -> new RuntimeException("Post not found"));

            AppUser user = userRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Comment comment = new Comment(text, post, user);
            return commentRepository.save(comment);
        } catch (Exception e) {
            log.error("Error occurred while commenting on post {}: {}", postId, e.getMessage(), e);
            throw new PostException("Failed to comment on post", e);
        }
    }

    @Override
    public void likeComment(String commentId, String userId) {
        try {
            Comment comment = commentRepository.findById(UUID.fromString(commentId))
                    .orElseThrow(() -> new RuntimeException("Comment not found"));

            AppUser user = userRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!comment.getLikedUsers().contains(user)) {
                comment.getLikedUsers().add(user);
                commentRepository.save(comment);
            }
        } catch (Exception e) {
            log.error("Error occurred while liking comment {}: {}", commentId, e.getMessage(), e);
            throw new PostException("Failed to like comment", e);
        }
    }

    @Override
    public void unlikeComment(String commentId, String userId) {
        try {
            Comment comment = commentRepository.findById(UUID.fromString(commentId))
                    .orElseThrow(() -> new RuntimeException("Comment not found"));

            AppUser user = userRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (comment.getLikedUsers().remove(user)) {
                commentRepository.save(comment);
            }
        } catch (Exception e) {
            log.error("Error occurred while unliking comment {}: {}", commentId, e.getMessage(), e);
            throw new PostException("Failed to unlike comment", e);
        }
    }
}
