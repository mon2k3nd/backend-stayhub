package com.stayhub.api.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String phoneNumber;
    private String currentPlan; // Dành cho gói dịch vụ của Chủ nhà (FREE, PRO, VIP)
    private String appType;     // 🌟 THÊM MỚI: Định danh Role tương ứng khi gộp chung 1 app (OWNER_APP, STAFF_APP, TENANT_APP)
}