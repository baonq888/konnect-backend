package com.konnectnet.core.post.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostRequest {
    private String content;
    private String visibility;
    private List<String> photoUrls;

    @Override
    public String toString() {
        return "PostRequest{" +
                "content='" + content + '\'' +
                ", visibility='" + visibility + '\'' +
                ", photoUrls=" + photoUrls +
                '}';
    }
}
