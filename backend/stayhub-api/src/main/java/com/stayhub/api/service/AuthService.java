package com.stayhub.api.service;

import com.stayhub.api.config.JwtUtil;
import com.stayhub.api.dto.response.AuthResponse;
import com.stayhub.api.dto.response.LoginRequest;
import com.stayhub.api.dto.response.RegisterRequest;
import com.stayhub.api.entity.Otp;
import com.stayhub.api.entity.User;
import com.stayhub.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu xác nhận không khớp");
        if (userRepository.existsByEmail(request.getEmail()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã được đăng ký");
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Số điện thoại đã được đăng ký");

        String otpTarget = request.getOtpChannel() == Otp.Channel.email
                ? request.getEmail() : request.getPhoneNumber();
        otpService.verifyAndMarkUsed(otpTarget, request.getOtpCode(), Otp.OtpType.register);

        User user = User.builder()
                .name(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roleId("TENANT").packageId("FREE")
                .status(User.UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);
        return buildResponse("Đăng ký thành công",
                jwtUtil.generateToken(user.getId(), user.getRoleId(), user.getPackageId()), user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        boolean isEmail = request.getEmailOrPhone().contains("@");
        User user = isEmail
                ? userRepository.findByEmail(request.getEmailOrPhone())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email/SĐT hoặc mật khẩu không đúng"))
                : userRepository.findByPhoneNumber(request.getEmailOrPhone())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email/SĐT hoặc mật khẩu không đúng"));

        if (user.getStatus() == User.UserStatus.LOCKED)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản đã bị khóa. Liên hệ hỗ trợ.");

        boolean valid;
        if (isBcryptHash(user.getPasswordHash())) {
            valid = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        } else {
            valid = request.getPassword().equals(user.getPasswordHash());
            if (valid) {
                user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
                userRepository.save(user);
            }
        }

        if (!valid)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email/SĐT hoặc mật khẩu không đúng");

        return buildResponse("Đăng nhập thành công",
                jwtUtil.generateToken(user.getId(), user.getRoleId(), user.getPackageId()), user);
    }

    private boolean isBcryptHash(String hash) {
        return hash != null && hash.matches("^\\$2[aby]\\$\\d+\\$.+");
    }

    private AuthResponse buildResponse(String message, String token, User user) {
        return AuthResponse.builder().message(message).token(token)
                .user(AuthResponse.UserDto.builder()
                        .id(user.getId())
                        .fullName(user.getName())
                        .email(user.getEmail())
                        .phone(user.getPhoneNumber())
                        .roleId(user.getRoleId())
                        .packageId(user.getPackageId())
                        .status(user.getStatus())
                        .build())
                .build();
    }
}