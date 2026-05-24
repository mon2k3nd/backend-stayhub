package com.stayhub.api.repository;

import com.stayhub.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    // 🌟 THÊM DÒNG NÀY VÀO ĐỂ HẾT LỖI findByPhoneNumber
    Optional<User> findByPhoneNumber(String phoneNumber);
}