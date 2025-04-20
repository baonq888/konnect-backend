package com.konnectnet.core.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class LoginResponse {

    @Schema(description = "JWT Access Token", example = "eyJhbGciOiJIUzI1NiIs...")
    private String access_token;

    @Schema(description = "JWT Refresh Token", example = "eyJhbGciOiJIUzI1NiIs...")
    private String refresh_token;
}
