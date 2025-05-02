package com.konnectnet.core.post.mapper;

import com.konnectnet.core.post.dto.response.CommentDTO;
import com.konnectnet.core.post.entity.Comment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")

public interface CommentMapper {
    CommentDTO toCommentDTO(Comment comment);
}
