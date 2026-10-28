package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "帖子分页查询请求")
public class PostPageRequest {

    @Schema(description = "页码，从1开始", defaultValue = "1")
    @Min(value = 1, message = "页码最小为1")
    private int page = 1;

    @Schema(description = "每页数量，最大50", defaultValue = "10")
    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 50, message = "每页数量最大为50")
    private int size = 10;

    @Schema(description = "帖子类型过滤：DISCUSSION/QUESTION/NOTICE/SHARE/ACTIVITY/LOST_FOUND/MARKET")
    @Pattern(regexp = "DISCUSSION|QUESTION|NOTICE|SHARE|ACTIVITY|LOST_FOUND|MARKET", message = "帖子类型不合法")
    private String type;

    @Schema(description = "关键词搜索（标题模糊匹配）")
    @Size(max = 50, message = "搜索关键词不能超过50字")
    private String keyword;

    @Schema(description = "排序字段：created_at/view_count/like_count/comment_count", defaultValue = "created_at")
    @Pattern(regexp = "created_at|view_count|like_count|comment_count", message = "排序字段不合法")
    private String sort = "created_at";

    @Schema(description = "排序方向：asc/desc", defaultValue = "desc")
    @Pattern(regexp = "asc|desc", message = "排序方向不合法")
    private String order = "desc";
}
