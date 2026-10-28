package com.campus.forum.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "评论（含子评论）")
public class CommentVO {

    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "帖子ID")
    private Long postId;

    @Schema(description = "作者用户ID")
    private Long userId;

    @Schema(description = "作者昵称")
    private String authorNickname;

    @Schema(description = "作者头像URL")
    private String authorAvatarUrl;

    @Schema(description = "父评论ID（顶级评论为null）")
    private Long parentCommentId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "当前用户是否已点赞")
    private Boolean isLikedByCurrentUser;

    @Schema(description = "发布时间")
    private LocalDateTime createdAt;

    @Schema(description = "子评论列表（仅顶级评论有此字段）")
    private List<CommentVO> replies;
}
