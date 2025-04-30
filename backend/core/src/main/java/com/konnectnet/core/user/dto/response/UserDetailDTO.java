package com.konnectnet.core.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserDetailDTO {
    private String bio;
    private String profilePictureUrl;
}
