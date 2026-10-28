package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "登录请求")
public class LoginRequest {

    @Schema(description = "用户名", example = "student001")
    @NotBlank(message = "用户名不能为空")
    @Size(max = 20, message = "用户名长度不能超过20位")
    private String username;

    @Schema(description = "密码", example = "Password@123")
    @NotBlank(message = "密码不能为空")
    @Size(max = 72, message = "密码长度不能超过72位")
    private String password;
}
