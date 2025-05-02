package com.konnectnet.core.post.mapper;

import com.konnectnet.core.auth.entity.AppUser;
import com.konnectnet.core.post.dto.response.PhotoDTO;
import com.konnectnet.core.post.dto.response.PostDTO;
import com.konnectnet.core.post.dto.response.PostLikeDTO;
import com.konnectnet.core.post.dto.response.PostShareDTO;
import com.konnectnet.core.post.entity.Photo;
import com.konnectnet.core.post.entity.Post;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PostMapper {

    @Mappings({
            @Mapping(source = "user.id", target = "userId"),
            @Mapping(source = "user.userDetail.profilePictureUrl", target = "profileImage"),
            @Mapping(source = "user.name", target = "userName"),
            @Mapping(source = "originalPost.id", target = "originalPostId"),
            @Mapping(source = "photos", target = "photoDTOS"),
            @Mapping(source = "comments", target = "commentDTOS"),
            @Mapping(source = "likedUsers", target = "postLikeDTOS"),
            @Mapping(source = "sharedPosts", target = "postShareDTOS")
    })
    PostDTO toPostDTO(Post post);

    PhotoDTO toPhotoDTO(Photo photo);

    @Mappings({
            @Mapping(source = "id", target = "userId"),
            @Mapping(source = "name", target = "userName"),
            @Mapping(source = "userDetail.profilePictureUrl", target = "profileImage")
    })
    PostLikeDTO toPostLikeDTO(AppUser user);

    @Mappings({
            @Mapping(source = "id", target = "userId"),
            @Mapping(source = "name", target = "userName"),
            @Mapping(source = "userDetail.profilePictureUrl", target = "profileImage")
    })
    PostShareDTO toPostShareDTO(AppUser user);

    default List<PostLikeDTO> mapLikedUsers(List<AppUser> users) {
        return users.stream()
                .map(this::toPostLikeDTO)
                .collect(Collectors.toList());
    }

    default List<PostShareDTO> mapSharedPosts(List<Post> sharedPosts) {
        return sharedPosts.stream()
                .map(Post::getUser)
                .map(this::toPostShareDTO)
                .collect(Collectors.toList());
    }


}