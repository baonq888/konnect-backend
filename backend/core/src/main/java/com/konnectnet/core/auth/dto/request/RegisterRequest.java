package com.konnectnet.core.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class RegisterRequest {
    @Schema(example = "John Doe")
    private String name;
    @Schema(example = "john@example.com")
    private String email;
    @Schema(example = "secret123")
    private String password;

}
