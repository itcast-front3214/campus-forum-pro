package com.campus.forum.controller;

import com.campus.forum.common.result.PageVO;
import com.campus.forum.common.result.Result;
import com.campus.forum.dto.ReportCreateRequest;
import com.campus.forum.dto.ReportHandleRequest;
import com.campus.forum.service.ReportService;
import com.campus.forum.vo.ReportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "举报模块", description = "举报帖子或评论，管理员处理举报")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Validated
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "提交举报（需登录）")
    @PostMapping
    public Result<ReportVO> createReport(@Valid @RequestBody ReportCreateRequest request) {
        return Result.success(reportService.createReport(request));
    }

    @Operation(summary = "处理举报（管理员/版主）")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    @PutMapping("/{reportId}/handle")
    public Result<Void> handleReport(@PathVariable Long reportId,
                                      @Valid @RequestBody ReportHandleRequest request) {
        reportService.handleReport(reportId, request);
        return Result.success();
    }

    @Operation(summary = "分页查看举报列表（管理员/版主）")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    @GetMapping
    public Result<PageVO<ReportVO>> listReports(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return Result.success(reportService.listReports(status, page, size));
    }
}
