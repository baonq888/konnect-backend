package com.konnectnet.core.post.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostRequest {
    private String content;
    private String visibility;
    private List<String> photoUrls;
}
