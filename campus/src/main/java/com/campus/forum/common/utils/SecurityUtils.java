package com.campus.forum.common.utils;

import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.common.result.ResultCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Spring Security 上下文工具类
 * 替代各 Service 中分散的 getCurrentUserIdRequired() 方法，消除代码重复
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * 获取当前登录用户 ID（必须已登录，否则抛异常）
     */
    public static Long getCurrentUserId() {
        Authentication auth = getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw BusinessException.of(ResultCode.UNAUTHORIZED, "请先登录");
        }
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            throw BusinessException.of(ResultCode.UNAUTHORIZED, "认证信息异常");
        }
    }

    /**
     * 获取当前用户 ID（未登录返回 null）
     */
    public static Long getCurrentUserIdOrNull() {
        try {
            return getCurrentUserId();
        } catch (BusinessException e) {
            return null;
        }
    }

    /**
     * 判断当前用户是否为管理员或版主
     */
    public static boolean isAdminOrModerator() {
        Authentication auth = getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN") || a.equals("ROLE_MODERATOR"));
    }

    /**
     * 判断当前用户是否为管理员
     */
    public static boolean isAdmin() {
        Authentication auth = getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));
    }

    private static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
