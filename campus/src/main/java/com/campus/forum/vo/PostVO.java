package com.campus.forum.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "帖子列表项")
public class PostVO {

    @Schema(description = "帖子ID")
    private Long id;

    @Schema(description = "作者用户ID")
    private Long userId;

    @Schema(description = "作者昵称")
    private String authorNickname;

    @Schema(description = "作者头像URL")
    private String authorAvatarUrl;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "帖子类型")
    private String type;

    @Schema(description = "审核状态")
    private String auditStatus;

    @Schema(description = "审核备注（仅流程列表中使用）")
    private String auditRemark;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "浏览量")
    private Integer viewCount;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "是否置顶：0/1")
    private Integer isPinned;

    @Schema(description = "发布时间")
    private LocalDateTime createdAt;
}
