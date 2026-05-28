package com.stayhub.api.dto.response;

import com.stayhub.api.entity.Otp;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendOtpRequest {
    @NotBlank(message = "target không được để trống")
    private String target;
    @NotNull(message = "channel phải là 'email' hoặc 'sms'")
    private Otp.Channel channel;
    @NotNull(message = "type phải là 'register' hoặc 'reset_password'")
    private Otp.OtpType type;
}