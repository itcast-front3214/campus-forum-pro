package com.campus.forum.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "帖子详情")
public class PostDetailVO extends PostVO {

    @Schema(description = "正文内容")
    private String content;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "当前用户是否已点赞（未登录时为null）")
    private Boolean isLikedByCurrentUser;

    @Schema(description = "最后更新时间")
    private LocalDateTime updatedAt;
}
