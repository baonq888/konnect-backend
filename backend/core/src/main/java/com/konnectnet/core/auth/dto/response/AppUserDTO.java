package com.konnectnet.core.auth.dto.response;

import com.konnectnet.core.user.dto.response.UserDetailDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class AppUserDTO {
    private UUID id;
    private String name;
    private String email;
    private UserDetailDTO userDetail;
}
