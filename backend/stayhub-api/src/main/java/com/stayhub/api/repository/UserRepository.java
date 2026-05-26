package com.stayhub.api.repository;

import com.stayhub.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.roleId = :role")
    List<User> findByRole(String role);

    @Query("SELECT u FROM User u WHERE u.accountStatus = 'LOCKED'")
    List<User> findLockedAccounts();

    @Query("SELECT u FROM User u WHERE (u.name LIKE %:q% OR u.phoneNumber LIKE %:q%) AND u.roleId = 'TENANT'")
    List<User> searchTenants(String q);
}
