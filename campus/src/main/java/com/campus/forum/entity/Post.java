package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("post")
public class Post {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String title;
    private String content;

    /** 帖子类型：DISCUSSION / QUESTION / NOTICE / SHARE */
    private String type;

    /** 审核状态：PENDING / REVIEWING / APPROVED / REJECTED */
    private String auditStatus;

    /** 标签，逗号分隔 */
    private String tags;

    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;

    /** 是否置顶：0=否，1=是 */
    private Integer isPinned;

    /** 审核备注（拒绝原因） */
    private String auditRemark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // ---- 联表查询结果（非数据库列）----
    @TableField(exist = false)
    private String authorNickname;

    @TableField(exist = false)
    private String authorAvatarUrl;
}
