package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "更新帖子请求（所有字段均可选，只更新非null字段）")
public class PostUpdateRequest {

    @Schema(description = "标题")
    @Size(min = 2, max = 200, message = "标题长度2-200字")
    private String title;

    @Schema(description = "正文内容")
    @Size(min = 10, max = 50000, message = "内容长度10-50000字")
    private String content;

    @Schema(description = "帖子类型：DISCUSSION/QUESTION/NOTICE/SHARE/ACTIVITY/LOST_FOUND/MARKET")
    @Pattern(regexp = "DISCUSSION|QUESTION|NOTICE|SHARE|ACTIVITY|LOST_FOUND|MARKET", message = "帖子类型不合法")
    private String type;

    @Schema(description = "标签，逗号分隔")
    @Size(max = 500, message = "标签总长度不超过500字")
    private String tags;
}
