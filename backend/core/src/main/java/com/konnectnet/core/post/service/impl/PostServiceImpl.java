package com.konnectnet.core.post.service.impl;

import com.konnectnet.core.auth.entity.AppUser;
import com.konnectnet.core.auth.repository.UserRepository;
import com.konnectnet.core.post.dto.request.PostRequest;
import com.konnectnet.core.post.dto.response.CommentDTO;
import com.konnectnet.core.post.dto.response.PostDTO;
import com.konnectnet.core.post.entity.Comment;
import com.konnectnet.core.post.entity.Photo;
import com.konnectnet.core.post.entity.Post;
import com.konnectnet.core.post.enums.Visibility;
import com.konnectnet.core.post.exception.PostException;
import com.konnectnet.core.post.mapper.CommentMapper;
import com.konnectnet.core.post.mapper.PostMapper;
import com.konnectnet.core.post.repository.CommentRepository;
import com.konnectnet.core.post.repository.PostRepository;
import com.konnectnet.core.post.service.PostService;
import com.konnectnet.core.search.LuceneSearchService;
import com.konnectnet.core.search.document.DocumentInfo;
import com.konnectnet.core.search.document.SearchResult;
import com.konnectnet.core.search.index.IndexService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final IndexService indexService;
    private final LuceneSearchService luceneSearchService;

    @Override
    @Transactional
    public PostDTO createPost(PostRequest request) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = new Post(
            request.getContent(), Visibility.valueOf(request.getVisibility())
        );

        List<Photo> photoList = new ArrayList<>();
        for (String url : request.getPhotoUrls()) {
            photoList.add(new Photo(url));
        }
        post.setPhotos(photoList);
        post.setUser(user);

        Post savedPost = postRepository.save(post);
        System.out.println("Before "+savedPost.getContent());

        // Index Post document to Lucence
        DocumentInfo documentInfo = new DocumentInfo(
                savedPost.getId().toString(),
                savedPost.getContent(),
                savedPost.getUser().getName()
        );
        indexService.indexDocument(documentInfo);

        return postMapper.toPostDTO(savedPost);

    }

    @Override
    public PostDTO getPostById(String postId) {

        UUID id = UUID.fromString(postId);
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with ID: " + postId));
        return postMapper.toPostDTO(post);

    }

    @Override
    public Page<PostDTO> searchPosts(String searchTerm, Pageable pageable) throws IOException {

        // Perform Lucene search
        List<SearchResult> searchResults = luceneSearchService.search(searchTerm, pageable.getPageNumber(), pageable.getPageSize());

        // Convert SearchResults to Post
        List<PostDTO> posts = new ArrayList<>();
        for (SearchResult result : searchResults) {
            Post post = postRepository.findById(result.getPostId())
                    .orElseThrow(() -> new PostException("Post not found for search result"));
            posts.add(postMapper.toPostDTO(post));
        }

        return new PageImpl<>(posts, pageable, searchResults.size());

    }

    @Override
    @Transactional
    public PostDTO updatePost(String postId, PostRequest request) {

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
        post = postRepository.save(post);
        return postMapper.toPostDTO(post);

    }

    @Override
    @Transactional
    public void deletePost(String postId) {

        // Fetch the post by ID
        UUID id = UUID.fromString(postId);
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with ID: " + postId));

        postRepository.delete(post);

        log.info("Post with ID {} deleted successfully", postId);

    }

    @Override
    public void likePost(String postId, String userId) {

        Post post = postRepository.findById(UUID.fromString(postId))
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        AppUser user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!post.getLikedUsers().contains(user)) {
            post.getLikedUsers().add(user);
            postRepository.save(post);
        }

    }

    @Override
    public void unlikePost(String postId, String userId) {

        Post post = postRepository.findById(UUID.fromString(postId))
                .orElseThrow(() -> new RuntimeException("Post not found"));

        AppUser user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (post.getLikedUsers().contains(user)) {
            post.getLikedUsers().remove(user);
            postRepository.save(post);
        }
    }

    @Override
    public PostDTO sharePost(String postId, String userId, String userContent) {

        Post originalPost = postRepository.findById(UUID.fromString(postId))
                .orElseThrow(() -> new RuntimeException("Post not found"));

        AppUser user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post sharedPost = new Post();
        sharedPost.setContent(userContent);
        sharedPost.setUser(user);
        sharedPost.setOriginalPost(originalPost);
        sharedPost.setVisibility(originalPost.getVisibility());

        sharedPost = postRepository.save(sharedPost);

        return postMapper.toPostDTO(sharedPost);


    }

    @Override
    public void unsharePost(String sharedPostId) {

        Post sharedPost = postRepository.findById(UUID.fromString(sharedPostId))
                .orElseThrow(() -> new RuntimeException("Shared post not found"));

        postRepository.delete(sharedPost);

    }

    @Override
    public CommentDTO commentOnPost(String postId, String userId, String text) {

        Post post = postRepository.findById(UUID.fromString(postId))
                .orElseThrow(() -> new RuntimeException("Post not found"));

        AppUser user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = new Comment(text, post, user);
        comment = commentRepository.save(comment);
        return commentMapper.toCommentDTO(comment);

    }

    @Override
    public void likeComment(String postId, String commentId, String userId) {
       postRepository.findById(UUID.fromString(postId))
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = commentRepository.findById(UUID.fromString(commentId))
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        AppUser user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!comment.getLikedUsers().contains(user)) {
            comment.getLikedUsers().add(user);
            commentRepository.save(comment);
        }

    }

    @Override
    public void unlikeComment(String postId, String commentId, String userId) {
        postRepository.findById(UUID.fromString(postId))
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = commentRepository.findById(UUID.fromString(commentId))
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        AppUser user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (comment.getLikedUsers().remove(user)) {
            commentRepository.save(comment);
        }

    }
}
