package com.konnectnet.core.user.mapper;

import com.konnectnet.core.auth.entity.AppUser;
import com.konnectnet.core.user.dto.response.UserDetailDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "userDetail.bio", target = "bio")
    @Mapping(source = "userDetail.profilePictureUrl", target = "profilePictureUrl")
    UserDetailDTO toUserDetailDTO(AppUser user);
}
