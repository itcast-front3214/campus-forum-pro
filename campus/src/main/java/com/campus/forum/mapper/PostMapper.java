package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.entity.Post;
import org.apache.ibatis.annotations.*;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 分页查询帖子（联表带作者信息）
     * ⚠️ sort/order 字段使用 XML 白名单控制，防止 SQL 注入
     */
    IPage<Post> selectPageWithAuthor(Page<Post> page,
                                     @Param("type") String type,
                                     @Param("keyword") String keyword,
                                     @Param("auditStatus") String auditStatus,
                                     @Param("sort") String sort,
                                     @Param("order") String order);

    /** 作者个人中心与审核工作台使用的流程分页；userId 为 null 时查询全站。 */
    IPage<Post> selectWorkflowPage(Page<Post> page,
                                   @Param("userId") Long userId,
                                   @Param("stage") String stage);

    @Select("SELECT p.*, u.nickname AS author_nickname, u.avatar_url AS author_avatar_url " +
            "FROM post p LEFT JOIN user u ON p.user_id = u.id WHERE p.id = #{id}")
    Post selectByIdWithAuthor(@Param("id") Long id);

    @Update("UPDATE post SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);

    @Update("UPDATE post SET comment_count = comment_count + 1 WHERE id = #{id}")
    int incrementCommentCount(@Param("id") Long id);

    @Update("UPDATE post SET comment_count = GREATEST(comment_count - 1, 0) WHERE id = #{id}")
    int decrementCommentCount(@Param("id") Long id);

    @Update("UPDATE post SET like_count = like_count + 1 WHERE id = #{id}")
    int incrementLikeCount(@Param("id") Long id);

    @Update("UPDATE post SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{id}")
    int decrementLikeCount(@Param("id") Long id);
}
