package com.stayhub.api.dto.response;

import com.stayhub.api.entity.Otp;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;
    @NotBlank @Email(message = "Email không hợp lệ")
    private String email;
    @NotBlank @Pattern(regexp = "^(0|\\+84)[3-9]\\d{8}$", message = "SĐT không hợp lệ")
    private String phoneNumber;
    @NotBlank @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự")
    private String password;
    @NotBlank(message = "Vui lòng xác nhận mật khẩu")
    private String confirmPassword;
    @NotBlank @Size(min = 6, max = 6)
    private String otpCode;
    @NotNull
    private Otp.Channel otpChannel;
}