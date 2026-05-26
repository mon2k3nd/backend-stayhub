package com.stayhub.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                // 1. Kích hoạt cấu hình CORS chuẩn chỉnh tách biệt
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 2. Vô hiệu hóa CSRF do ứng dụng Flutter chạy qua REST API (Stateless)
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // 🟢 SỬA LỖI 403: Cho phép các gói Pre-flight request (OPTIONS) từ Flutter đi qua trước
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                        // Public APIs (Không cần Token)
                        .requestMatchers(
                                "/api/auth/**",
                                "/auth/**",
                                "/uploads/**",
                                "/images/**",
                                "/api/rooms/**",
                                "/rooms/**"
                        ).permitAll()

                        // ADMIN
                        .requestMatchers(
                                "/api/admin/**",
                                "/admin/**"
                        ).hasAnyAuthority("ADMIN", "ROLE_ADMIN")

                        // OWNER
                        .requestMatchers(
                                "/api/owner/**",
                                "/owner/**"
                        ).hasAnyAuthority(
                                "ADMIN",
                                "ROLE_ADMIN",
                                "OWNER",
                                "ROLE_OWNER"
                        )

                        // STAFF
                        .requestMatchers(
                                "/api/staff/**",
                                "/staff/**"
                        ).hasAnyAuthority(
                                "ADMIN",
                                "ROLE_ADMIN",
                                "OWNER",
                                "ROLE_OWNER",
                                "STAFF",
                                "ROLE_STAFF"
                        )

                        // Các API còn lại bắt buộc đăng nhập
                        .anyRequest().authenticated()
                )

                .sessionManagement(sess ->
                        sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authenticationProvider(authenticationProvider)

                // 🟢 Đưa bộ lọc kiểm tra JWT vào vị trí sau bước lọc cấu hình Http cơ bản
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 🟢 CẤU HÌNH CORS CHUẨN CHO SPRING BOOT 3.X VÀ FLUTTER EMULATOR:
        // Thay vì dùng .setAllowedOrigins(List.of("*")) kèm credentials false dễ gây lỗi bắt tay HTTP,
        // sử dụng AllowedOriginPatterns cho phép nhận dạng IP ảo 10.0.2.2 từ giả lập Android một cách an toàn.
        configuration.setAllowedOriginPatterns(List.of("*"));

        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With"
        ));

        configuration.setExposedHeaders(List.of("Authorization"));

        // 🟢 Đổi lại thành true để hỗ trợ các gói request mang header tùy biến từ thư viện Dio
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}