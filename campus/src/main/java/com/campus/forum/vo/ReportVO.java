package com.campus.forum.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "举报记录")
public class ReportVO {

    private Long id;
    private Long reporterId;
    private String reporterNickname;
    private String targetType;
    private Long targetId;
    private String reason;
    private String status;
    private Long handlerId;
    private String handlerNickname;
    private String handleRemark;
    private LocalDateTime createdAt;
    private LocalDateTime handledAt;
}
