package com.stayhub.api.config;

import com.stayhub.api.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);
        try {
            String email = jwtService.extractUsername(jwt);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);

                // 🌟 BƯỚC SỬA ĐỔI QUAN TRỌNG: Tự động giải mã Payload của chuỗi JWT để bóc tách Role
                String tokenRole = null;
                try {
                    String[] chunks = jwt.split("\\.");
                    if (chunks.length > 1) {
                        String payload = new String(java.util.Base64.getUrlDecoder().decode(chunks[1]));
                        if (payload.contains("\"role\":\"")) {
                            tokenRole = payload.substring(payload.indexOf("\"role\":\"") + 8);
                            tokenRole = tokenRole.substring(0, tokenRole.indexOf("\""));
                        }
                    }
                } catch (Exception ignored) {
                    // Bỏ qua nếu có lỗi băm chuỗi thủ công
                }

                List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                // Ưu tiên nạp quyền bóc tách được trực tiếp từ Token JWT trước
                if (tokenRole != null && !tokenRole.isEmpty()) {
                    if (!tokenRole.startsWith("ROLE_")) {
                        tokenRole = "ROLE_" + tokenRole.toUpperCase();
                    }
                    authorities.add(new SimpleGrantedAuthority(tokenRole));
                } else if (userDetails.getAuthorities() != null) {
                    // Phương án dự phòng: Đọc danh sách quyền mặc định từ database của hệ thống
                    userDetails.getAuthorities().forEach(grantedAuthority -> {
                        String authStr = grantedAuthority.getAuthority();
                        if (!authStr.startsWith("ROLE_")) {
                            authStr = "ROLE_" + authStr.toUpperCase();
                        }
                        authorities.add(new SimpleGrantedAuthority(authStr));
                    });
                }

                // In Log sạch ra console giám sát luồng dữ liệu thực tế
                System.out.println("\n============ [STAYHUB SECURITY CLEAR] ============");
                System.out.println("👉 Request URL: " + request.getRequestURI());
                System.out.println("👉 Tài khoản xác thực: " + email);
                System.out.println("👉 Quyền hạn chính thức duyệt nạp: " + authorities);
                System.out.println("=====================================================\n");

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        authorities
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi xảy ra tại bộ lọc bảo mật JWT Filter: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}