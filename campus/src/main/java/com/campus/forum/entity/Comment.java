package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;
    private Long userId;

    /** 父评论ID，null 表示顶级评论 */
    private Long parentCommentId;

    private String content;
    private Integer likeCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // ---- 联表查询（非列） ----
    @TableField(exist = false)
    private String authorNickname;

    @TableField(exist = false)
    private String authorAvatarUrl;
}
