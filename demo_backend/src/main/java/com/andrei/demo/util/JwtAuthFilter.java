// demo_backend/src/main/java/com/andrei/demo/util/JwtAuthFilter.java
package com.andrei.demo.util;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path   = request.getRequestURI();
        String method = request.getMethod();
        String authHeader = request.getHeader("Authorization");
        boolean hasToken  = authHeader != null && authHeader.startsWith("Bearer ");

        boolean isLogin         = "/login".equals(path);
        boolean isPreflight     = "OPTIONS".equalsIgnoreCase(method);
        boolean isForgotPass    = path.startsWith("/person/forgot-password");
        boolean isResetPass     = path.startsWith("/person/reset-password");

        boolean isPublicRegistration =
                "/person".equals(path) && "POST".equalsIgnoreCase(method) && !hasToken;

        if (isLogin || isPublicRegistration || isPreflight || isForgotPass || isResetPass) {
            log.debug("Skipping JWT filter for: {} {}", method, path);
            filterChain.doFilter(request, response);
            return;
        }

        if (!hasToken) {
            log.error("Missing Authorization header for: {} {}", method, path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = authHeader.substring(7);
        try {
            if (!jwtUtil.checkClaims(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String userId = jwtUtil.getUserIdFromToken(token);
            String role   = jwtUtil.getRoleFromToken(token);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId, null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            log.error("Invalid JWT: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}