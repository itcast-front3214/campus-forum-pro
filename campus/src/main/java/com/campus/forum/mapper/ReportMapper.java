package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.entity.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReportMapper extends BaseMapper<Report> {

    @Select("<script>" +
            "SELECT r.*, " +
            "  u1.nickname AS reporter_nickname, " +
            "  u2.nickname AS handler_nickname " +
            "FROM report r " +
            "LEFT JOIN user u1 ON r.reporter_id = u1.id " +
            "LEFT JOIN user u2 ON r.handler_id = u2.id " +
            "<where>" +
            "  <if test='status != null'>AND r.status = #{status}</if>" +
            "</where>" +
            "ORDER BY r.created_at DESC" +
            "</script>")
    IPage<Report> selectPageWithUsers(Page<Report> page, @Param("status") String status);
}
