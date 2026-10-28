package com.campus.forum.controller;

import com.campus.forum.common.result.PageVO;
import com.campus.forum.common.result.Result;
import com.campus.forum.dto.AuditRequest;
import com.campus.forum.service.PostService;
import com.campus.forum.vo.PostVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "内容审核", description = "管理员和版主的内容审核工作流")
@RestController
@RequestMapping("/api/moderation/posts")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
public class ModerationController {

    private final PostService postService;

    @Operation(summary = "查询审核工作台帖子")
    @GetMapping
    public Result<PageVO<PostVO>> list(
            @RequestParam(defaultValue = "WAITING")
            @Pattern(regexp = "WAITING|REVIEWING|COMPLETED", message = "流程阶段不合法") String stage,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return Result.success(postService.getReviewPosts(stage, page, size));
    }

    @Operation(summary = "领取帖子进入审核中")
    @PutMapping("/{id}/start")
    public Result<Void> start(@PathVariable Long id) {
        postService.startReview(id);
        return Result.success();
    }

    @Operation(summary = "提交审核决定")
    @PutMapping("/{id}/decision")
    public Result<Void> decide(@PathVariable Long id, @Valid @RequestBody AuditRequest request) {
        postService.auditPost(id, request);
        return Result.success();
    }
}
