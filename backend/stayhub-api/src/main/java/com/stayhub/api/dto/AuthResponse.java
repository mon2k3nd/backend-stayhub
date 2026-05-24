package com.stayhub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private Long id; // 🌟 Thêm trường ID để truyền xuống Mobile App
    private String token;
    private String email;
    private String role;
    private String plan;
}