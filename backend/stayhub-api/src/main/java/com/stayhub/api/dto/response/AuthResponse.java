package com.stayhub.api.dto.response;

import com.stayhub.api.entity.User;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthResponse {
    private String message;
    private String token;
    private UserDto user;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UserDto {
        private Long id;
        private String fullName;
        private String email;
        private String phone;
        private String roleId;
        private String packageId;
        private User.UserStatus status;
        private Boolean isEmailVerified;
        private Boolean isPhoneVerified;
    }
}