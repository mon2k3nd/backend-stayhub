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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 1. Cổng API Đăng nhập/Đăng ký và xem phòng công khai cho phép truy cập tự do không cần Token
                        .requestMatchers("/api/auth/**", "/auth/**").permitAll()
                        .requestMatchers("/uploads/**", "/images/**").permitAll()
                        .requestMatchers("/api/rooms", "/api/rooms/**", "/rooms/**").permitAll()

                        // 2. 🌟 SỬA ĐỔI QUAN TRỌNG: Mở rộng Ant-Style matchers để bao quát toàn bộ cụm API con lồng nhau và Path Variables
                        .requestMatchers("/api/admin", "/api/admin/**", "/api/admin/*************").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/owner", "/api/owner/**", "/api/owner/*************").hasAnyAuthority("ROLE_ADMIN", "ROLE_OWNER")
                        .requestMatchers("/api/staff", "/api/staff/**", "/api/staff/*************").hasAnyAuthority("ROLE_ADMIN", "ROLE_OWNER", "ROLE_STAFF")

                        // 3. Các hành động tài nguyên hệ thống thông thường yêu cầu phải Đăng nhập hợp lệ
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * CẤU HÌNH CORS TOÀN DIỆN CHO THIẾT BỊ DI ĐỘNG (REAL DEVICE & EMULATOR)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With", "Cache-Control", "Origin"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}