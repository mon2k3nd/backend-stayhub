package com.stayhub.api.service.impl;

import com.stayhub.api.config.JwtUtil;
import com.stayhub.api.dto.response.LoginRequest;
import com.stayhub.api.dto.response.LoginResponse;
import com.stayhub.api.dto.response.RegisterRequest;
import com.stayhub.api.dto.response.UserProfileResponse;
import com.stayhub.api.entity.User;
import com.stayhub.api.exception.ResourceNotFoundException;
import com.stayhub.api.repository.UserRepository;
import com.stayhub.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public abstract class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse register(RegisterRequest req) {

        if (userRepository.existsByPhoneNumber(req.getPhoneNumber())) {
            throw new IllegalStateException("Số điện thoại đã được đăng ký!");
        }

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalStateException("Email đã được đăng ký!");
        }

        User user = User.builder()
                .name(req.getEmail())
                .phoneNumber(req.getPhoneNumber())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .roleId("TENANT")
                .packageId("FREE")
                .status(User.UserStatus.ACTIVE)
                .build();

        User saved = userRepository.save(user);

        String token = jwtUtil.generateToken(
                saved.getId(),
                saved.getRoleId(),
                saved.getPackageId()
        );

        return LoginResponse.builder()
                .accessToken(token)
                .userId(saved.getId())
                .name(saved.getName())
                .phoneNumber(saved.getPhoneNumber())
                .roleId(saved.getRoleId())
                .packageId(saved.getPackageId())
                .accountStatus(saved.getStatus().name())
                .build();
    }

    public LoginResponse login(LoginRequest req) {

        User user = userRepository.findByPhoneNumber(req.getPassword())
                .orElseThrow(() ->
                        new IllegalStateException("Số điện thoại không tồn tại!")
                );

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalStateException("Mật khẩu không đúng!");
        }

        if (user.getStatus() == User.UserStatus.LOCKED) {
            throw new IllegalStateException(
                    "Tài khoản của bạn đã bị khóa!"
            );
        }

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getRoleId(),
                user.getPackageId()
        );

        return LoginResponse.builder()
                .accessToken(token)
                .userId(user.getId())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .roleId(user.getRoleId())
                .packageId(user.getPackageId())
                .accountStatus(user.getStatus().name())
                .build();
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy user ID: " + id
                        )
                );
    }

    @Override
    public UserProfileResponse getProfileById(Long id) {
        return UserProfileResponse.from(getById(id));
    }

    @Override
    public UserProfileResponse updateProfile(
            Long userId,
            Map<String, String> fields
    ) {

        User user = getById(userId);

        if (fields.containsKey("fullName")) {
            user.setName(fields.get("fullName"));
        }

        if (fields.containsKey("email")) {

            String newEmail = fields.get("email");

            if (!newEmail.equals(user.getEmail())
                    && userRepository.existsByEmail(newEmail)) {

                throw new IllegalStateException(
                        "Email đã được sử dụng!"
                );
            }

            user.setEmail(newEmail);
        }

        return UserProfileResponse.from(
                userRepository.save(user)
        );
    }

    @Override
    public void changePassword(
            Long userId,
            String oldPassword,
            String newPassword
    ) {

        User user = getById(userId);

        if (!passwordEncoder.matches(
                oldPassword,
                user.getPasswordHash()
        )) {

            throw new IllegalStateException(
                    "Mật khẩu cũ không đúng!"
            );
        }

        user.setPasswordHash(
                passwordEncoder.encode(newPassword)
        );

        userRepository.save(user);
    }

    @Override
    public void lockAccount(Long userId) {

        User user = getById(userId);

        user.setStatus(User.UserStatus.LOCKED);

        userRepository.save(user);
    }

    @Override
    public void unlockAccount(Long userId) {

        User user = getById(userId);

        user.setStatus(User.UserStatus.ACTIVE);

        userRepository.save(user);
    }
    @Override
    public User updateFcmToken(Long userId, String fcmToken) {
        User user = getById(userId);
        user.setFcmToken(fcmToken);
        return userRepository.save(user);
    }
}