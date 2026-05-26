package com.stayhub.api.repository;

import com.stayhub.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 🌟 Tìm kiếm người dùng bằng Số điện thoại làm định danh đăng nhập
    Optional<User> findByPhoneNumber(String phoneNumber);
}