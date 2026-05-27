package com.stayhub.api.service;

import com.stayhub.api.dto.request.LoginRequest;
import com.stayhub.api.dto.request.RegisterRequest;
import com.stayhub.api.dto.response.LoginResponse;
import com.stayhub.api.dto.response.UserProfileResponse;
import com.stayhub.api.entity.User;

import java.util.Map;

public interface UserService {
    LoginResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    User getById(Long id);
    UserProfileResponse getProfileById(Long id);
    UserProfileResponse updateProfile(Long userId, Map<String, String> fields);
    void changePassword(Long userId, String oldPassword, String newPassword);
    User updateFcmToken(Long userId, String fcmToken);
    void lockAccount(Long userId);
    void unlockAccount(Long userId);
}
