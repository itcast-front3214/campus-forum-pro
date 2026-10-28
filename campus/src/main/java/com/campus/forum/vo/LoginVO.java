package com.campus.forum.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginVO {

    @Schema(description = "JWT Token，请求时放在 Authorization: Bearer <token>")
    private String token;

    @Schema(description = "当前用户信息")
    private UserVO user;
}
