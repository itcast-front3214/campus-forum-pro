package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("report")
public class Report {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long reporterId;

    /** 举报目标类型：POST / COMMENT */
    private String targetType;

    private Long targetId;
    private String reason;

    /** 状态：PENDING / RESOLVED / DISMISSED */
    private String status;

    private Long handlerId;
    private String handleRemark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    private LocalDateTime handledAt;
}
