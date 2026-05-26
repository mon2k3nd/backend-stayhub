package com.stayhub.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterRequest {
    @NotBlank(message = "Tên không được trống")
    private String name;

    @NotBlank(message = "Số điện thoại không được trống")
    private String phoneNumber;

    @NotBlank(message = "Mật khẩu không được trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    private String cccdNumber;
    private String hometown;
    private String gender; // MALE | FEMALE | OTHER
}
