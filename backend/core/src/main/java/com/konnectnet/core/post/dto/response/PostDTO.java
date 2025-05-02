package com.konnectnet.core.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PostDTO {
    private UUID id;
    private String content;
    private Date uploadDate;
    private String visibility;
    private UUID userId;
    private String profileImage;
    private String userName;
    private UUID originalPostId;
    private List<PhotoDTO> photoDTOS;
    private List<CommentDTO> commentDTOS;
}
