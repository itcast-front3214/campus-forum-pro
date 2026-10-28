package com.campus.forum.service;

import com.campus.forum.common.result.PageVO;
import com.campus.forum.dto.LoginRequest;
import com.campus.forum.dto.RegisterRequest;
import com.campus.forum.dto.UserProfileUpdateRequest;
import com.campus.forum.vo.LoginVO;
import com.campus.forum.vo.UserVO;

public interface UserService {
    UserVO register(RegisterRequest request);
    LoginVO login(LoginRequest request);
    UserVO getCurrentUser();
    UserVO updateProfile(UserProfileUpdateRequest request);
    UserVO getUserById(Long id);
    void banUser(Long id, boolean ban);
    PageVO<UserVO> listUsers(int page, int size);
}
