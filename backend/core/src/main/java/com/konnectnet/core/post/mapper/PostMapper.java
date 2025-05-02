package com.konnectnet.core.post.mapper;

import com.konnectnet.core.post.dto.response.CommentDTO;
import com.konnectnet.core.post.dto.response.PostDTO;
import com.konnectnet.core.post.entity.Comment;
import com.konnectnet.core.post.entity.Post;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PostMapper {

    @Mappings({
            @Mapping(source = "user.id", target = "userId"),
            @Mapping(source = "user.userDetail.profilePictureUrl", target = "profileImage"),
            @Mapping(source = "user.name", target = "userName"),
            @Mapping(source = "originalPost.id", target = "originalPostId"),
            @Mapping(source = "photos", target = "photoDTOS"),
            @Mapping(source = "comments", target = "commentDTOS")
    })
    PostDTO toPostDTO(Post post);


}