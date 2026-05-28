package com.stayhub.api.service;

import com.stayhub.api.dto.response.SendOtpRequest;
import com.stayhub.api.dto.response.SendOtpResponse;
import com.stayhub.api.entity.Otp;
import com.stayhub.api.repository.OtpRepository;
import com.stayhub.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    private static final int OTP_EXPIRY_MINUTES = 10;

    public SendOtpResponse sendOtp(SendOtpRequest request) {
        if (request.getType() == Otp.OtpType.register) {
            if (request.getChannel() == Otp.Channel.email) {
                if (userRepository.existsByEmail(request.getTarget()))
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email này đã được sử dụng");
            } else {
                if (userRepository.existsByPhoneNumber(request.getTarget()))
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Số điện thoại này đã được sử dụng");
            }
        }

        String code = generateCode();
        otpRepository.save(Otp.builder()
                .target(request.getTarget()).channel(request.getChannel())
                .type(request.getType()).code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .build());

        log.info("[OTP DEV] channel={} target={} code={}", request.getChannel(), request.getTarget(), code);

        SendOtpResponse.SendOtpResponseBuilder builder = SendOtpResponse.builder()
                .message("Mã OTP đã được gửi đến " +
                        (request.getChannel() == Otp.Channel.email ? "email" : "số điện thoại") + " của bạn")
                .expiresInMinutes(OTP_EXPIRY_MINUTES);

        if (!"production".equals(activeProfile)) builder.devOtp(code);
        return builder.build();
    }

    public Otp verifyAndMarkUsed(String target, String code, Otp.OtpType type) {
        Otp otp = otpRepository.findValidOtp(target, code, type, LocalDateTime.now())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Mã OTP không hợp lệ hoặc đã hết hạn"));
        otp.setUsedAt(LocalDateTime.now());
        return otpRepository.save(otp);
    }

    private String generateCode() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }
}