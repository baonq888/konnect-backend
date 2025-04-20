package com.konnectnet.core.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @Schema(example = "john@example.com")
    private String email;
    @Schema(example = "secret123")
    private String password;
}
