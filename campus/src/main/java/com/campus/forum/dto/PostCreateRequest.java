package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建帖子请求")
public class PostCreateRequest {

    @Schema(description = "标题", example = "求推荐图书馆附近的奶茶店")
    @NotBlank(message = "标题不能为空")
    @Size(min = 2, max = 200, message = "标题长度2-200字")
    private String title;

    @Schema(description = "正文内容")
    @NotBlank(message = "内容不能为空")
    @Size(min = 10, max = 50000, message = "内容长度10-50000字")
    private String content;

    @Schema(description = "帖子类型：DISCUSSION/QUESTION/NOTICE/SHARE/ACTIVITY/LOST_FOUND/MARKET", example = "DISCUSSION")
    @NotBlank(message = "帖子类型不能为空")
    @Pattern(regexp = "DISCUSSION|QUESTION|NOTICE|SHARE|ACTIVITY|LOST_FOUND|MARKET", message = "帖子类型不合法")
    private String type;

    @Schema(description = "标签，逗号分隔", example = "校园生活,美食")
    @Size(max = 500, message = "标签总长度不超过500字")
    private String tags;
}
