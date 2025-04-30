package com.konnectnet.core.auth.mapper;

import com.konnectnet.core.auth.entity.AppUser;
import com.konnectnet.core.user.entity.UserDetail;
import com.konnectnet.core.auth.dto.response.AppUserDTO;
import com.konnectnet.core.user.dto.response.UserDetailDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppUserMapper {

    @Mapping(source = "userDetail.bio", target = "userDetail.bio")
    @Mapping(source = "userDetail.profilePictureUrl", target = "userDetail.profilePictureUrl")
    AppUserDTO toDto(AppUser user);

    UserDetailDTO toDto(UserDetail detail);
}
