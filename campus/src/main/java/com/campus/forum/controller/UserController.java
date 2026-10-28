package com.campus.forum.controller;

import com.campus.forum.common.result.PageVO;
import com.campus.forum.common.result.Result;
import com.campus.forum.dto.LoginRequest;
import com.campus.forum.dto.RegisterRequest;
import com.campus.forum.dto.UserProfileUpdateRequest;
import com.campus.forum.service.PostService;
import com.campus.forum.service.UserService;
import com.campus.forum.vo.LoginVO;
import com.campus.forum.vo.PostVO;
import com.campus.forum.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户模块", description = "注册、登录、个人信息管理")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;
    private final PostService postService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    @Operation(summary = "用户登录", description = "返回 JWT Token，后续请求携带 Authorization: Bearer <token>")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/me")
    public Result<UserVO> currentUser() {
        return Result.success(userService.getCurrentUser());
    }

    @Operation(summary = "更新个人资料（昵称/头像）")
    @PutMapping("/me")
    public Result<UserVO> updateProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        return Result.success(userService.updateProfile(request));
    }

    @Operation(summary = "查看用户公开信息")
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @Operation(summary = "查看用户发布的帖子")
    @GetMapping("/{id}/posts")
    public Result<PageVO<PostVO>> getUserPosts(
            @PathVariable Long id,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") @Min(1) int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return Result.success(postService.getPostsByUser(id, page, size));
    }

    @Operation(summary = "封禁/解封用户（管理员专用）")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/ban")
    public Result<Void> banUser(
            @PathVariable Long id,
            @Parameter(description = "true=封禁, false=解封") @RequestParam(defaultValue = "true") boolean ban) {
        userService.banUser(id, ban);
        return Result.success();
    }

    @Operation(summary = "分页查看所有用户（管理员专用）")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Result<PageVO<UserVO>> listUsers(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return Result.success(userService.listUsers(page, size));
    }
}
