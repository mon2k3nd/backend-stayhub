package com.stayhub.api.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private Long id;
    private String token;
    private String phoneNumber;
    private String role;
    private String plan;
    private String name;
    private String email;
    private String cccdNumber;
    private String hometown;
    private String gender;
}