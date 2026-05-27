package com.stayhub.api.service.impl;

import com.stayhub.api.config.JwtUtil;
import com.stayhub.api.dto.request.LoginRequest;
import com.stayhub.api.dto.request.RegisterRequest;
import com.stayhub.api.dto.response.LoginResponse;
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
    public UserProfileResponse getProfileById(Long id) {
        return UserProfileResponse.from(getById(id));
    }

    @Override
    public UserProfileResponse updateProfile(Long userId, Map<String, String> fields) {
        User user = getById(userId);
        if (fields.containsKey("name")) user.setName(fields.get("name"));
        if (fields.containsKey("hometown")) user.setHometown(fields.get("hometown"));
        if (fields.containsKey("gender")) user.setGender(fields.get("gender"));
        if (fields.containsKey("avatarUrl")) user.setAvatarUrl(fields.get("avatarUrl"));
        if (fields.containsKey("email")) {
            String newEmail = fields.get("email");
            if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new IllegalStateException("Email đã được sử dụng bởi tài khoản khác!");
            }
            user.setEmail(newEmail);
        }
        return UserProfileResponse.from(userRepository.save(user));
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự!");
        }
        User user = getById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalStateException("Mật khẩu cũ không đúng!");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
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
