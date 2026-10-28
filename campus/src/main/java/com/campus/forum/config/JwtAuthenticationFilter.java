package com.campus.forum.config;

import com.campus.forum.common.utils.JwtUtils;
import com.campus.forum.entity.User;
import com.campus.forum.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器（每个请求只执行一次）
 * 从 Authorization Header 解析 JWT，设置 SecurityContext
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = jwtUtils.extractToken(authHeader);

        if (StringUtils.hasText(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            Claims claims = jwtUtils.parseTokenSafely(token);
            if (claims != null) {
                Long userId = jwtUtils.getUserId(claims);
                User user = userMapper.selectById(userId);
                // 使用数据库中的最新状态和角色，使封禁与降权立即生效。
                if (user != null && Integer.valueOf(1).equals(user.getStatus())) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            String.valueOf(userId), null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
                    );
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.debug("JWT 认证通过，userId={}, role={}", userId, user.getRole());
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
