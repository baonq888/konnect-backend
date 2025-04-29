package com.konnectnet.core.post.controller;

import com.konnectnet.core.post.dto.request.PostRequest;
import com.konnectnet.core.post.entity.Comment;
import com.konnectnet.core.post.entity.Post;
import com.konnectnet.core.post.service.PostService;
import com.konnectnet.core.post.exception.PostException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "Posts", description = "Endpoints for managing posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(
            summary = "Create a new post",
            description = "Creates a new post with the provided content, visibility, and photos"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Post created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Post.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping
    public ResponseEntity<Post> createPost(@Valid @RequestBody PostRequest postRequest) {
        try {
            Post post = postService.createPost(postRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(post);
        } catch (PostException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(
            summary = "Get a post by ID",
            description = "Retrieves the post with the specified ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Post found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Post.class))
            ),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @GetMapping("/{postId}")
    public ResponseEntity<Post> getPostById(
            @Parameter(description = "The ID of the post to retrieve") @PathVariable String postId) {
        try {
            Post post = postService.getPostById(postId);
            return ResponseEntity.status(HttpStatus.OK).body(post);
        } catch (PostException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @Operation(
            summary = "Search for posts",
            description = "Search for posts based on a search term"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Search results returned",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Post.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid search term")
    })
    @GetMapping
    public ResponseEntity<Page<Post>> searchPosts(
            @Parameter(description = "Search term for post content") @RequestParam String searchTerm,
            @Parameter(description = "Current page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int limit) {

        Pageable pageable = PageRequest.of(page, limit);
        try {
            Page<Post> posts = postService.searchPosts(searchTerm, pageable);
            return ResponseEntity.status(HttpStatus.OK).body(posts);
        } catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(
            summary = "Update a post",
            description = "Updates the content, visibility, and photos of an existing post"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Post updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Post.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @PutMapping("/{postId}")
    public ResponseEntity<Post> updatePost(
            @Parameter(description = "The ID of the post to update") @PathVariable String postId,
            @Valid @RequestBody PostRequest postRequest) {
        try {
            Post post = postService.updatePost(postId, postRequest);
            return ResponseEntity.status(HttpStatus.OK).body(post);
        } catch (PostException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(
            summary = "Delete a post",
            description = "Deletes the post with the specified ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Post deleted successfully"
            ),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(
            @Parameter(description = "The ID of the post to delete") @PathVariable String postId) {
        try {
            postService.deletePost(postId);
            return ResponseEntity.status(HttpStatus.OK).body("Post deleted successfully");
        } catch (PostException e) {
            return new ResponseEntity<>("Failed to delete post", HttpStatus.NOT_FOUND);
        }
    }

    @Operation(
            summary = "Like a post",
            description = "Likes the specified post by a user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post liked successfully"),
            @ApiResponse(responseCode = "404", description = "Post or user not found")
    })
    @PostMapping("/{postId}/like")
    public ResponseEntity<String> likePost(
            @PathVariable String postId,
            @RequestParam String userId) {
        try {
            postService.likePost(postId, userId);
            return ResponseEntity.ok("Post liked successfully");
        } catch (PostException e) {
            return new ResponseEntity<>("Failed to like post", HttpStatus.NOT_FOUND);
        }
    }

    @Operation(
            summary = "Unlike a post",
            description = "Unlikes the specified post by a user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post unliked successfully"),
            @ApiResponse(responseCode = "404", description = "Post or user not found")
    })
    @PostMapping("/{postId}/unlike")
    public ResponseEntity<String> unlikePost(
            @PathVariable String postId,
            @RequestParam String userId) {
        try {
            postService.unlikePost(postId, userId);
            return ResponseEntity.ok("Post unliked successfully");
        } catch (PostException e) {
            return new ResponseEntity<>("Failed to unlike post", HttpStatus.NOT_FOUND);
        }
    }

    @Operation(
            summary = "Share a post",
            description = "Shares an existing post with optional user content"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Post shared successfully"),
            @ApiResponse(responseCode = "404", description = "Original post or user not found")
    })
    @PostMapping("/{postId}/share")
    public ResponseEntity<Post> sharePost(
            @PathVariable String postId,
            @RequestParam String userId,
            @RequestBody(required = false) String content) {
        try {
            Post sharedPost = postService.sharePost(postId, userId, content);
            return ResponseEntity.status(HttpStatus.CREATED).body(sharedPost);
        } catch (PostException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @Operation(
            summary = "Unshare a shared post",
            description = "Deletes a post that was shared by a user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Shared post removed successfully"),
            @ApiResponse(responseCode = "404", description = "Post not found or not a shared post")
    })
    @DeleteMapping("/{postId}/unshare")
    public ResponseEntity<String> unsharePost(@PathVariable String postId) {
        try {
            postService.unsharePost(postId);
            return ResponseEntity.ok("Shared post removed successfully");
        } catch (PostException e) {
            return new ResponseEntity<>("Failed to unshare post", HttpStatus.NOT_FOUND);
        }
    }

    @Operation(
            summary = "Comment on a post",
            description = "Adds a comment to a post by a specific user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comment added successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Comment.class))),
            @ApiResponse(responseCode = "404", description = "Post or user not found")
    })
    @PostMapping("/{postId}/comment")
    public ResponseEntity<Comment> commentOnPost(
            @PathVariable String postId,
            @RequestParam String userId,
            @RequestBody String text) {
        try {
            Comment comment = postService.commentOnPost(postId, userId, text);
            return ResponseEntity.status(HttpStatus.CREATED).body(comment);
        } catch (PostException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @Operation(
            summary = "Like a comment",
            description = "Likes a specific comment by a user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment liked successfully"),
            @ApiResponse(responseCode = "404", description = "Comment or user not found")
    })
    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<String> likeComment(
            @PathVariable String commentId,
            @RequestParam String userId) {
        try {
            postService.likeComment(commentId, userId);
            return ResponseEntity.ok("Comment liked successfully");
        } catch (PostException e) {
            return new ResponseEntity<>("Failed to like comment", HttpStatus.NOT_FOUND);
        }

    }

    @Operation(
            summary = "Unlike a comment",
            description = "Removes a like from a comment by a user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment unliked successfully"),
            @ApiResponse(responseCode = "404", description = "Comment or user not found")
    })
    @PostMapping("/comments/{commentId}/unlike")
    public ResponseEntity<String> unlikeComment(
            @PathVariable String commentId,
            @RequestParam String userId) {
        try {
            postService.unlikeComment(commentId, userId);
            return ResponseEntity.ok("Comment unliked successfully");
        } catch (PostException e) {
            return new ResponseEntity<>("Failed to unlike comment", HttpStatus.NOT_FOUND);
        }
    }
}
