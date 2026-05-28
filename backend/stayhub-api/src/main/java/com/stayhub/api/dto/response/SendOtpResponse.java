package com.stayhub.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SendOtpResponse {
    private String message;
    private Integer expiresInMinutes;
    @JsonProperty("__dev_otp")
    private String devOtp;
}