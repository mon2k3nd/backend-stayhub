package com.stayhub.api.dto;

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

    public AuthResponse() {
    }

    public AuthResponse(
            Long id,
            String token,
            String phoneNumber,
            String role,
            String plan,
            String name,
            String email
    ) {

        this.id = id;
        this.token = token;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.plan = plan;
        this.name = name;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCccdNumber() {
        return cccdNumber;
    }

    public void setCccdNumber(String cccdNumber) {
        this.cccdNumber = cccdNumber;
    }

    public String getHometown() {
        return hometown;
    }

    public void setHometown(String hometown) {
        this.hometown = hometown;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}