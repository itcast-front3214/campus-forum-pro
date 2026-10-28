package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "审核请求")
public class AuditRequest {

    @Schema(description = "审核结果：APPROVED / REJECTED", example = "APPROVED")
    @NotBlank(message = "审核状态不能为空")
    @Pattern(regexp = "APPROVED|REJECTED", message = "审核状态只能是 APPROVED 或 REJECTED")
    private String status;

    @Schema(description = "审核备注（拒绝时建议填写原因）", example = "内容违规")
    private String remark;
}
