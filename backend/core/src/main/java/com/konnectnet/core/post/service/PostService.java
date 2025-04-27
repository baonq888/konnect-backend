package com.konnectnet.core.post.service;

import com.konnectnet.core.post.dto.request.PostRequest;
import com.konnectnet.core.post.entity.Post;


public interface PostService {
    Post createPost(PostRequest request);
    Post getPostById(String postId);
    Post updatePost(String postId, PostRequest request);
    void deletePost(String postId);
}
