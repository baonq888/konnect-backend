package com.konnectnet.core.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PostShareDTO {
    private UUID userId;
    private String profileImage;
    private String userName;
}
