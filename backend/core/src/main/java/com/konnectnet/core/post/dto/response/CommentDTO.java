package com.konnectnet.core.post.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Setter
@Getter
public class CommentDTO {
    private UUID id;
    private String text;
    private Date createdAt;
    private UUID userId;
    private int likeCount;
}
