package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "用户注册请求")
public class RegisterRequest {

    @Schema(description = "用户名（4-20位字母数字下划线）", example = "student001")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度4-20位")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @Schema(description = "密码（8-30位）", example = "Password@123")
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 30, message = "密码长度8-30位")
    private String password;

    @Schema(description = "昵称（可选）", example = "小明")
    @Size(max = 30, message = "昵称不超过30位")
    private String nickname;

    @Schema(description = "邮箱（可选）", example = "student@campus.edu")
    @jakarta.validation.constraints.Email(message = "邮箱格式不正确")
    private String email;
}
