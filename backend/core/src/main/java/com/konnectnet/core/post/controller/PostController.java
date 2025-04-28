package com.konnectnet.core.post.controller;

import com.konnectnet.core.post.dto.request.PostRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

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
}
