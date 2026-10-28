package com.campus.forum.config;

import com.campus.forum.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security UserDetails 实现，携带完整用户信息
 */
@Getter
public class ForumUserDetails implements UserDetails {

    private final User user;
    private final List<GrantedAuthority> authorities;

    public ForumUserDetails(User user) {
        this.user = user;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
    }

    /** 返回 userId 字符串，与 JWT subject 保持一致 */
    @Override
    public String getUsername() {
        return String.valueOf(user.getId());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return user.getStatus() == 1; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return user.getStatus() == 1; }
}
