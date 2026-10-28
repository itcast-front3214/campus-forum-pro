package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment_like")
public class CommentLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long commentId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
