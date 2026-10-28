package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "举报请求")
public class ReportCreateRequest {

    @Schema(description = "举报目标类型：POST / COMMENT", example = "POST")
    @NotBlank(message = "举报目标类型不能为空")
    @Pattern(regexp = "POST|COMMENT", message = "目标类型只能是 POST 或 COMMENT")
    private String targetType;

    @Schema(description = "举报目标ID", example = "1")
    @NotNull(message = "举报目标ID不能为空")
    private Long targetId;

    @Schema(description = "举报原因", example = "内容涉及广告")
    @NotBlank(message = "举报原因不能为空")
    @Size(min = 5, max = 500, message = "举报原因5-500字")
    private String reason;
}
