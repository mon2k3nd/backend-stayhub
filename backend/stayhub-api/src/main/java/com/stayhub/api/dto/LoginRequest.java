package com.stayhub.api.dto;

public class LoginRequest {

    private String email;
    private String phoneNumber;
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String email, String phoneNumber, String password) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

    // ===== EMAIL =====
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // ===== PHONE =====
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // ===== PASSWORD =====
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}