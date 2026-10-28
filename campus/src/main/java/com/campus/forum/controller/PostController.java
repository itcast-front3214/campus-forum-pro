package com.campus.forum.controller;

import com.campus.forum.common.result.PageVO;
import com.campus.forum.common.result.Result;
import com.campus.forum.dto.PostCreateRequest;
import com.campus.forum.dto.PostPageRequest;
import com.campus.forum.dto.PostUpdateRequest;
import com.campus.forum.service.PostService;
import com.campus.forum.vo.PostDetailVO;
import com.campus.forum.vo.PostVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "帖子模块", description = "帖子的增删改查、点赞、审核")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Validated
public class PostController {

    private final PostService postService;

    @Operation(summary = "分页查询帖子列表（支持类型过滤和关键词搜索）")
    @GetMapping
    public Result<PageVO<PostVO>> listPosts(@Valid PostPageRequest request) {
        return Result.success(postService.listPosts(request));
    }

    @Operation(summary = "热门帖子排行榜（按浏览量）")
    @GetMapping("/hot")
    public Result<PageVO<PostVO>> hotPosts(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return Result.success(postService.getHotPosts(page, size));
    }

    @Operation(summary = "获取帖子详情（自动累加浏览量，同IP1小时内去重）")
    @GetMapping("/{id}")
    public Result<PostDetailVO> getPostDetail(@PathVariable Long id, HttpServletRequest request) {
        // 不直接信任客户端可伪造的转发头；可信代理应由容器统一配置 ForwardedHeaderStrategy。
        return Result.success(postService.getPostDetail(id, request.getRemoteAddr()));
    }

    @Operation(summary = "发布帖子（需登录，发布后进入待审核状态）")
    @PostMapping
    public Result<PostVO> createPost(@Valid @RequestBody PostCreateRequest request) {
        return Result.success(postService.createPost(request));
    }

    @Operation(summary = "编辑帖子（仅作者/管理员/版主）")
    @PutMapping("/{id}")
    public Result<PostVO> updatePost(@PathVariable Long id,
                                     @Valid @RequestBody PostUpdateRequest request) {
        return Result.success(postService.updatePost(id, request));
    }

    @Operation(summary = "删除帖子（仅作者/管理员/版主）")
    @DeleteMapping("/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return Result.success();
    }

    @Operation(summary = "点赞/取消点赞帖子（需登录）")
    @PostMapping("/{id}/like")
    public Result<Void> toggleLike(@PathVariable Long id) {
        postService.toggleLike(id);
        return Result.success();
    }

    @Operation(summary = "查看当前用户自己的帖子流程（含未公开内容）")
    @GetMapping("/mine")
    public Result<PageVO<PostVO>> myPosts(
            @RequestParam(defaultValue = "WAITING")
            @Pattern(regexp = "WAITING|REVIEWING|COMPLETED", message = "流程阶段不合法") String stage,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return Result.success(postService.getMyPosts(stage, page, size));
    }

}
