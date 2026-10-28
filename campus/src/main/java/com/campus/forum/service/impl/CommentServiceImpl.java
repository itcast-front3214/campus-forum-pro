package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.common.result.PageVO;
import com.campus.forum.common.result.ResultCode;
import com.campus.forum.common.utils.SecurityUtils;
import com.campus.forum.dto.CommentCreateRequest;
import com.campus.forum.entity.Comment;
import com.campus.forum.entity.CommentLike;
import com.campus.forum.entity.Post;
import com.campus.forum.mapper.CommentLikeMapper;
import com.campus.forum.mapper.CommentMapper;
import com.campus.forum.mapper.PostMapper;
import com.campus.forum.service.CommentService;
import com.campus.forum.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final CommentLikeMapper commentLikeMapper;
    private final PostMapper postMapper;

    @Override
    public PageVO<CommentVO> listComments(Long postId, int page, int size) {
        ensurePostVisible(postId);
        // 1. 查顶级评论（全部，再内存分页）
        List<Comment> topLevel = baseMapper.selectTopLevelByPostId(postId);

        int total = topLevel.size();
        int from = (page - 1) * size;
        int to = Math.min(from + size, total);
        List<Comment> paged = from >= total ? Collections.emptyList() : topLevel.subList(from, to);

        // 2. 批量查当前页的所有子评论
        List<Long> parentIds = paged.stream().map(Comment::getId).collect(Collectors.toList());
        Map<Long, List<CommentVO>> repliesMap = new HashMap<>();
        if (!parentIds.isEmpty()) {
            parentIds.forEach(pid -> {
                List<Comment> replies = baseMapper.selectRepliesByParentId(pid);
                repliesMap.put(pid, replies.stream().map(this::toVO).collect(Collectors.toList()));
            });
        }

        // 3. 获取当前用户已点赞的评论（批量，避免 N+1 查询）
        Long currentUserId = SecurityUtils.getCurrentUserIdOrNull();

        List<CommentVO> records = paged.stream().map(c -> {
            CommentVO vo = toVO(c);
            if (currentUserId != null) {
                boolean liked = commentLikeMapper.exists(
                        new LambdaQueryWrapper<CommentLike>()
                                .eq(CommentLike::getUserId, currentUserId)
                                .eq(CommentLike::getCommentId, c.getId())
                );
                vo.setIsLikedByCurrentUser(liked);
            }
            vo.setReplies(repliesMap.getOrDefault(c.getId(), Collections.emptyList()));
            return vo;
        }).collect(Collectors.toList());

        long pages = (long) Math.ceil((double) total / size);
        return new PageVO<>((long) page, (long) size, (long) total, pages, records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVO createComment(Long postId, CommentCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 验证帖子存在
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw BusinessException.of(ResultCode.POST_NOT_FOUND, "帖子不存在");
        }
        if (!"APPROVED".equals(post.getAuditStatus())
                && !post.getUserId().equals(userId) && !SecurityUtils.isAdminOrModerator()) {
            throw BusinessException.of(ResultCode.POST_NOT_FOUND, "帖子不存在");
        }

        // 验证父评论（如果是回复）
        if (request.getParentCommentId() != null) {
            Comment parent = getById(request.getParentCommentId());
            if (parent == null || !parent.getPostId().equals(postId)) {
                throw BusinessException.of(ResultCode.COMMENT_NOT_FOUND, "父评论不存在");
            }
        }

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setParentCommentId(request.getParentCommentId());
        comment.setLikeCount(0);
        save(comment);

        // 更新帖子评论数
        postMapper.incrementCommentCount(postId);

        log.info("评论创建成功: id={}, postId={}, userId={}", comment.getId(), postId, userId);
        return toVO(baseMapper.selectByIdWithAuthor(comment.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        Comment comment = getById(commentId);
        if (comment == null) {
            throw BusinessException.of(ResultCode.COMMENT_NOT_FOUND, "评论不存在");
        }

        Long userId = SecurityUtils.getCurrentUserId();
        if (!comment.getUserId().equals(userId) && !SecurityUtils.isAdminOrModerator()) {
            throw BusinessException.of(ResultCode.COMMENT_ACCESS_DENIED, "无权删除他人评论");
        }

        removeById(commentId);
        postMapper.decrementCommentCount(comment.getPostId());
        log.info("评论删除成功: id={}", commentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleLike(Long commentId) {
        Comment comment = getById(commentId);
        if (comment == null) {
            throw BusinessException.of(ResultCode.COMMENT_NOT_FOUND, "评论不存在");
        }
        Long userId = SecurityUtils.getCurrentUserId();

        CommentLike existing = commentLikeMapper.selectOne(
                new LambdaQueryWrapper<CommentLike>()
                        .eq(CommentLike::getUserId, userId)
                        .eq(CommentLike::getCommentId, commentId)
        );

        if (existing != null) {
            commentLikeMapper.deleteById(existing.getId());
            // 减少点赞数（防止为负）
            update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Comment>()
                    .eq(Comment::getId, commentId)
                    .setSql("like_count = GREATEST(like_count - 1, 0)"));
        } else {
            CommentLike like = new CommentLike();
            like.setUserId(userId);
            like.setCommentId(commentId);
            commentLikeMapper.insert(like);
            update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Comment>()
                    .eq(Comment::getId, commentId)
                    .setSql("like_count = like_count + 1"));
        }
    }

    private CommentVO toVO(Comment c) {
        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(c, vo);
        return vo;
    }

    private void ensurePostVisible(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || (!"APPROVED".equals(post.getAuditStatus())
                && !post.getUserId().equals(SecurityUtils.getCurrentUserIdOrNull())
                && !SecurityUtils.isAdminOrModerator())) {
            throw BusinessException.of(ResultCode.POST_NOT_FOUND, "帖子不存在");
        }
    }
}
