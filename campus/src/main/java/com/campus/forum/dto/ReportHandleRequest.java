package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "处理举报请求")
public class ReportHandleRequest {

    @Schema(description = "处理结果：RESOLVED / DISMISSED", example = "RESOLVED")
    @NotBlank(message = "处理状态不能为空")
    @Pattern(regexp = "RESOLVED|DISMISSED", message = "处理状态只能是 RESOLVED 或 DISMISSED")
    private String status;

    @Schema(description = "处理备注")
    private String remark;
}
