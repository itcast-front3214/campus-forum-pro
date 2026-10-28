package com.campus.forum.controller;

import com.campus.forum.common.result.PageVO;
import com.campus.forum.common.result.Result;
import com.campus.forum.dto.CommentCreateRequest;
import com.campus.forum.service.CommentService;
import com.campus.forum.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "评论模块", description = "评论的增删查和点赞")
@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
@Validated
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "分页获取帖子评论（含子评论，树形结构）")
    @GetMapping
    public Result<PageVO<CommentVO>> listComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return Result.success(commentService.listComments(postId, page, size));
    }

    @Operation(summary = "发表评论（需登录）")
    @PostMapping
    public Result<CommentVO> createComment(@PathVariable Long postId,
                                            @Valid @RequestBody CommentCreateRequest request) {
        return Result.success(commentService.createComment(postId, request));
    }

    @Operation(summary = "删除评论（仅评论作者/管理员/版主）")
    @DeleteMapping("/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long postId,
                                       @PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return Result.success();
    }

    @Operation(summary = "点赞/取消点赞评论（需登录）")
    @PostMapping("/{commentId}/like")
    public Result<Void> toggleLike(@PathVariable Long postId,
                                    @PathVariable Long commentId) {
        commentService.toggleLike(commentId);
        return Result.success();
    }
}
