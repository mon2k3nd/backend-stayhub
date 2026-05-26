package com.stayhub.api.service.impl;

import com.stayhub.api.config.JwtUtil;
import com.stayhub.api.dto.request.LoginRequest;
import com.stayhub.api.dto.request.RegisterRequest;
import com.stayhub.api.dto.response.LoginResponse;
import com.stayhub.api.entity.User;
import com.stayhub.api.exception.ResourceNotFoundException;
import com.stayhub.api.repository.UserRepository;
import com.stayhub.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse register(RegisterRequest req) {
        if (userRepository.existsByPhoneNumber(req.getPhoneNumber())) {
            throw new IllegalStateException("Số điện thoại đã được đăng ký!");
        }

        User user = User.builder()
                .name(req.getName())
                .phoneNumber(req.getPhoneNumber())
                .email("user_" + req.getPhoneNumber() + "@stayhub.com")
                .password(passwordEncoder.encode(req.getPassword()))
                .roleId("TENANT")
                .packageId("FREE")
                .accountStatus("ACTIVE")
                .cccdNumber(req.getCccdNumber())
                .hometown(req.getHometown())
                .gender(req.getGender())
                .build();

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getId(), saved.getRoleId(), saved.getPackageId());

        return LoginResponse.builder()
                .accessToken(token)
                .userId(saved.getId())
                .name(saved.getName())
                .phoneNumber(saved.getPhoneNumber())
                .roleId(saved.getRoleId())
                .packageId(saved.getPackageId())
                .accountStatus(saved.getAccountStatus())
                .build();
    }

    @Override
    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByPhoneNumber(req.getPhoneNumber())
                .orElseThrow(() -> new IllegalStateException("Số điện thoại không tồn tại!"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalStateException("Mật khẩu không đúng!");
        }

        if ("LOCKED".equals(user.getAccountStatus())) {
            throw new IllegalStateException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ ADMIN!");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getRoleId(), user.getPackageId());

        return LoginResponse.builder()
                .accessToken(token)
                .userId(user.getId())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .roleId(user.getRoleId())
                .packageId(user.getPackageId())
                .accountStatus(user.getAccountStatus())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user ID: " + id));
    }

    @Override
    public User updateFcmToken(Long userId, String fcmToken) {
        User user = getById(userId);
        user.setFcmToken(fcmToken);
        return userRepository.save(user);
    }

    @Override
    public void lockAccount(Long userId) {
        User user = getById(userId);
        user.setAccountStatus("LOCKED");
        userRepository.save(user);
    }

    @Override
    public void unlockAccount(Long userId) {
        User user = getById(userId);
        user.setAccountStatus("ACTIVE");
        userRepository.save(user);
    }
}
