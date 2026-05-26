package com.stayhub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data                  // 🌟 Quan trọng: Tự động sinh Getter/Setter bao gồm cả getName() và getPhoneNumber()
@NoArgsConstructor     // Tạo constructor không tham số bắt buộc cho Jackson map JSON
@AllArgsConstructor    // Tạo constructor đầy đủ tham số
public class RegisterRequest {
    private String name;
    private String password;
    private String phoneNumber;
    private String appType;
}