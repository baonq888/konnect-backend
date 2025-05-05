package com.konnectnet.core.auth.service;

import com.konnectnet.core.auth.dto.request.RegisterRequest;
import com.konnectnet.core.auth.dto.response.AppUserDTO;
import com.konnectnet.core.auth.entity.AppUser;

public interface AuthService {
    AppUserDTO saveUser(RegisterRequest request);
    AppUser getUser(String email);
    void saveRole(String roleName);
}
