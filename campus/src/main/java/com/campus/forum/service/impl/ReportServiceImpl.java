package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.common.result.PageVO;
import com.campus.forum.common.result.ResultCode;
import com.campus.forum.common.utils.SecurityUtils;
import com.campus.forum.dto.ReportCreateRequest;
import com.campus.forum.dto.ReportHandleRequest;
import com.campus.forum.entity.Report;
import com.campus.forum.mapper.CommentMapper;
import com.campus.forum.mapper.PostMapper;
import com.campus.forum.mapper.ReportMapper;
import com.campus.forum.service.ReportService;
import com.campus.forum.vo.ReportVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    private final PostMapper postMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportVO createReport(ReportCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 验证举报目标存在
        validateTarget(request.getTargetType(), request.getTargetId());

        // 防止重复举报
        boolean isDuplicate = baseMapper.selectCount(new LambdaQueryWrapper<Report>()
                .eq(Report::getReporterId, userId)
                .eq(Report::getTargetType, request.getTargetType())
                .eq(Report::getTargetId, request.getTargetId())
                .eq(Report::getStatus, "PENDING")
        ) > 0;
        if (isDuplicate) {
            throw BusinessException.of(ResultCode.REPORT_DUPLICATE, "您已举报过此内容，请等待处理");
        }

        Report report = new Report();
        report.setReporterId(userId);
        report.setTargetType(request.getTargetType());
        report.setTargetId(request.getTargetId());
        report.setReason(request.getReason());
        report.setStatus("PENDING");
        save(report);

        log.info("举报提交成功: id={}, type={}, targetId={}", report.getId(), request.getTargetType(), request.getTargetId());
        return toVO(report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleReport(Long reportId, ReportHandleRequest request) {
        Report report = getById(reportId);
        if (report == null) {
            throw BusinessException.of(ResultCode.REPORT_NOT_FOUND, "举报记录不存在");
        }

        Long handlerId = SecurityUtils.getCurrentUserId();
        report.setStatus(request.getStatus());
        report.setHandlerId(handlerId);
        report.setHandleRemark(request.getRemark());
        report.setHandledAt(LocalDateTime.now());
        updateById(report);

        log.info("举报处理完成: id={}, status={}, handlerId={}", reportId, request.getStatus(), handlerId);
    }

    @Override
    public PageVO<ReportVO> listReports(String status, int page, int size) {
        IPage<Report> result = baseMapper.selectPageWithUsers(new Page<>(page, size), status);
        List<ReportVO> records = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageVO<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), records);
    }

    private void validateTarget(String targetType, Long targetId) {
        boolean exists = switch (targetType) {
            case "POST"    -> postMapper.selectById(targetId) != null;
            case "COMMENT" -> commentMapper.selectById(targetId) != null;
            default        -> false;
        };
        if (!exists) {
            throw BusinessException.of(ResultCode.REPORT_TARGET_NOT_FOUND, "举报目标不存在");
        }
    }

    private ReportVO toVO(Report r) {
        ReportVO vo = new ReportVO();
        BeanUtils.copyProperties(r, vo);
        return vo;
    }
}
