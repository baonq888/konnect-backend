package com.konnectnet.core.post.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class PostRequest {
    private String content;
    private String visibility;
    private List<String> photoUrls;
}
