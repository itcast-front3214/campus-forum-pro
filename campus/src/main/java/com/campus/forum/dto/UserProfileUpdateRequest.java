package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "更新个人资料请求")
public class UserProfileUpdateRequest {

    @Schema(description = "昵称")
    @Size(min = 1, max = 30, message = "昵称长度1-30位")
    private String nickname;

    @Schema(description = "头像URL")
    @Size(max = 500, message = "头像URL长度不超过500")
    @Pattern(regexp = "^$|^https?://[^\\s]+$", message = "头像必须是有效的 http(s) 地址")
    private String avatarUrl;
}
