package com.konnectnet.core.post.service;

import com.konnectnet.core.post.dto.request.PostRequest;
import com.konnectnet.core.post.dto.response.CommentDTO;
import com.konnectnet.core.post.dto.response.PostDTO;
import com.konnectnet.core.post.entity.Comment;
import com.konnectnet.core.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;

public interface PostService {
    PostDTO createPost(PostRequest request);
    PostDTO getPostById(String postId);
    Page<PostDTO> searchPosts(String searchTerm, Pageable pageable) throws IOException;
    PostDTO updatePost(String postId, PostRequest request);
    void deletePost(String postId);
    void likePost(String postId, String userId);
    void unlikePost(String postId, String userId);
    PostDTO sharePost(String postId, String userId, String userContent);
    void unsharePost(String sharedPostId);
    CommentDTO commentOnPost(String postId, String userId, String text);
    void likeComment(String commentId, String userId);
    void unlikeComment(String commentId, String userId);
}
