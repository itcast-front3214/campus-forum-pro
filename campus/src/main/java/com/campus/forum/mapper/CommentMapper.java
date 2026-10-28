package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Select("SELECT c.*, u.nickname AS author_nickname, u.avatar_url AS author_avatar_url " +
            "FROM comment c LEFT JOIN user u ON c.user_id = u.id " +
            "WHERE c.post_id = #{postId} AND c.parent_comment_id IS NULL " +
            "ORDER BY c.created_at ASC")
    List<Comment> selectTopLevelByPostId(@Param("postId") Long postId);

    @Select("SELECT c.*, u.nickname AS author_nickname, u.avatar_url AS author_avatar_url " +
            "FROM comment c LEFT JOIN user u ON c.user_id = u.id " +
            "WHERE c.parent_comment_id = #{parentId} " +
            "ORDER BY c.created_at ASC")
    List<Comment> selectRepliesByParentId(@Param("parentId") Long parentId);

    @Select("SELECT c.*, u.nickname AS author_nickname, u.avatar_url AS author_avatar_url " +
            "FROM comment c LEFT JOIN user u ON c.user_id = u.id " +
            "WHERE c.id = #{id}")
    Comment selectByIdWithAuthor(@Param("id") Long id);
}
