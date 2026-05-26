package com.stayhub.api.config;

import com.stayhub.api.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;

@Configuration
public class ApplicationConfig {

    private final UserRepository userRepository;

    // Constructor Injection thuần thay cho Lombok để tránh lỗi không nhận diện bean
    public ApplicationConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Do hệ thống đăng nhập bằng Số điện thoại (identifier), biến 'username' ở đây chính là Số điện thoại
        return username -> userRepository.findByPhoneNumber(username)
                .map(user -> {
                    String password = user.getPassword();

                    // Lấy role_id từ Entity User (Mặc định nếu null là TENANT)
                    String roleName = user.getRoleId() != null ? user.getRoleId() : "TENANT";
                    String roleAuthority = "ROLE_" + roleName.toUpperCase();

                    System.out.println("====== [STAYHUB SECURITY CONFIG] ======");
                    System.out.println("👉 Đang nạp Security Context cho SĐT: " + username);
                    System.out.println("👉 Quyền hạn chính thức được cấp: " + roleAuthority);
                    System.out.println("=======================================");

                    return new org.springframework.security.core.userdetails.User(
                            username,
                            password,
                            List.of(new SimpleGrantedAuthority(roleAuthority))
                    );
                })
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản với số điện thoại: " + username));
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(List.of(authenticationProvider()));
    }
}