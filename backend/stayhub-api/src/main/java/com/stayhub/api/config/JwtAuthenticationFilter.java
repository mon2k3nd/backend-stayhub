package com.stayhub.api.config;

import com.stayhub.api.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 🟢 THAY ĐỔI CHÍ MẠNG: Bỏ qua kiểm tra Token JWT đối với các API Public (/api/auth)
        String servletPath = request.getServletPath();
        if (servletPath.contains("/api/auth") || servletPath.contains("/auth")) {
            filterChain.doFilter(request, response);
            return; // Dừng xử lý filter tại đây, nhường quyền cho Controller
        }

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
                String tokenRole = jwtService.extractRole(jwt);

                List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                if (tokenRole != null && !tokenRole.isEmpty()) {
                    String pureRole = tokenRole.replace("ROLE_", "").toUpperCase();
                    authorities.add(new SimpleGrantedAuthority(pureRole));
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + pureRole));
                }

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                authorities
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (Exception e) {
            System.err.println("JWT ERROR: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}