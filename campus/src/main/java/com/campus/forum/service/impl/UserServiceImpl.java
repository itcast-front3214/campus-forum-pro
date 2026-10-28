package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.common.result.PageVO;
import com.campus.forum.common.result.ResultCode;
import com.campus.forum.common.utils.JwtUtils;
import com.campus.forum.common.utils.SecurityUtils;
import com.campus.forum.dto.LoginRequest;
import com.campus.forum.dto.RegisterRequest;
import com.campus.forum.dto.UserProfileUpdateRequest;
import com.campus.forum.entity.User;
import com.campus.forum.mapper.UserMapper;
import com.campus.forum.service.UserService;
import com.campus.forum.vo.LoginVO;
import com.campus.forum.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(RegisterRequest request) {
        // 用户名唯一性检查
        if (baseMapper.selectByUsername(request.getUsername()) != null) {
            throw BusinessException.of(ResultCode.USERNAME_EXISTS, "用户名已存在");
        }
        // 邮箱唯一性检查
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && baseMapper.selectByEmail(request.getEmail()) != null) {
            throw BusinessException.of(ResultCode.EMAIL_EXISTS, "邮箱已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setRole("STUDENT");
        user.setStatus(1);
        save(user);

        log.info("新用户注册成功: username={}", user.getUsername());
        return toVO(user);
    }

    @Override
    public LoginVO login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (DisabledException e) {
            throw BusinessException.of(ResultCode.USER_DISABLED, "账号已被封禁");
        } catch (BadCredentialsException e) {
            throw BusinessException.of(ResultCode.WRONG_PASSWORD, "用户名或密码错误");
        }

        User user = baseMapper.selectByUsername(request.getUsername());
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

        log.info("用户登录成功: username={}", user.getUsername());
        return new LoginVO(token, toVO(user));
    }

    @Override
    public UserVO getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = getById(userId);
        if (user == null) {
            throw BusinessException.of(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
        return toVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateProfile(UserProfileUpdateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = getById(userId);
        if (user == null) {
            throw BusinessException.of(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        updateById(user);
        return toVO(user);
    }

    @Override
    public UserVO getUserById(Long id) {
        User user = getById(id);
        if (user == null) {
            throw BusinessException.of(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
        return toVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void banUser(Long id, boolean ban) {
        User user = getById(id);
        if (user == null) {
            throw BusinessException.of(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
        if (id.equals(SecurityUtils.getCurrentUserId())) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "不能封禁当前登录账号");
        }
        if ("ADMIN".equals(user.getRole())) {
            throw BusinessException.of(ResultCode.FORBIDDEN, "不能通过此接口封禁管理员");
        }
        user.setStatus(ban ? 0 : 1);
        updateById(user);
        log.info("用户{}封禁状态变更: username={}, banned={}", id, user.getUsername(), ban);
    }

    @Override
    public PageVO<UserVO> listUsers(int page, int size) {
        IPage<User> result = page(new Page<>(page, size),
                new LambdaQueryWrapper<User>().orderByDesc(User::getCreatedAt));
        List<UserVO> records = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return toPageVO(result, records);
    }

    // ============ 私有工具方法 ============

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    private <T, R> PageVO<R> toPageVO(IPage<T> page, List<R> records) {
        return new PageVO<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getPages(), records);
    }
}
