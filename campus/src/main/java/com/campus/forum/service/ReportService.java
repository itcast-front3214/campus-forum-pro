package com.campus.forum.service;

import com.campus.forum.common.result.PageVO;
import com.campus.forum.dto.ReportCreateRequest;
import com.campus.forum.dto.ReportHandleRequest;
import com.campus.forum.vo.ReportVO;

public interface ReportService {
    ReportVO createReport(ReportCreateRequest request);
    void handleReport(Long reportId, ReportHandleRequest request);
    PageVO<ReportVO> listReports(String status, int page, int size);
}
