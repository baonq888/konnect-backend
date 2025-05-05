package com.konnectnet.core.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class UserDetailDTO {
    private UUID id;
    private String name;
    private String email;
    private String bio;
    private String profilePictureUrl;
}
