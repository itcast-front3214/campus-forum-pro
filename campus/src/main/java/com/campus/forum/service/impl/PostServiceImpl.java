package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.common.result.PageVO;
import com.campus.forum.common.result.ResultCode;
import com.campus.forum.common.utils.RedisUtils;
import com.campus.forum.common.utils.SecurityUtils;
import com.campus.forum.dto.AuditRequest;
import com.campus.forum.dto.PostCreateRequest;
import com.campus.forum.dto.PostPageRequest;
import com.campus.forum.dto.PostUpdateRequest;
import com.campus.forum.entity.Post;
import com.campus.forum.entity.PostLike;
import com.campus.forum.mapper.PostLikeMapper;
import com.campus.forum.mapper.PostMapper;
import com.campus.forum.service.PostService;
import com.campus.forum.vo.PostDetailVO;
import com.campus.forum.vo.PostVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final PostLikeMapper postLikeMapper;
    private final RedisUtils redisUtils;

    @Value("${forum.post-cache-ttl:10}")
    private long postCacheTtl;

    @Value("${forum.view-dedup-hours:1}")
    private long viewDedupHours;

    @Value("${forum.hot-posts-cache-ttl:5}")
    private long hotPostsCacheTtl;

    // Redis Key 前缀常量（防止 magic string 散落各处）
    private static final String KEY_VIEW_DEDUP = "dedup:view:post:%d:ip:%s";
    private static final String KEY_HOT_POSTS  = "cache:hot_posts:%d:%d";

    @Override
    public PageVO<PostVO> listPosts(PostPageRequest request) {
        IPage<Post> result = baseMapper.selectPageWithAuthor(
                new Page<>(request.getPage(), request.getSize()),
                request.getType(),
                request.getKeyword(),
                "APPROVED",
                request.getSort(),
                request.getOrder()
        );
        return toPageVO(result);
    }

    @Override
    public PageVO<PostVO> getHotPosts(int page, int size) {
        // 热榜使用 Redis 缓存，减少 DB 压力
        String cacheKey = String.format(KEY_HOT_POSTS, page, size);

        @SuppressWarnings("unchecked")
        PageVO<PostVO> cached = redisUtils.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        IPage<Post> result = baseMapper.selectPageWithAuthor(
                new Page<>(page, size),
                null, null, "APPROVED",
                "view_count", "desc"
        );
        PageVO<PostVO> vo = toPageVO(result);
        redisUtils.set(cacheKey, vo, hotPostsCacheTtl, TimeUnit.MINUTES);
        return vo;
    }

    @Override
    public PostDetailVO getPostDetail(Long id, String clientIp) {
        Post post = baseMapper.selectByIdWithAuthor(id);
        if (post == null) {
            throw BusinessException.of(ResultCode.POST_NOT_FOUND, "帖子不存在");
        }
        Long currentUserId = SecurityUtils.getCurrentUserIdOrNull();
        if (!"APPROVED".equals(post.getAuditStatus())
                && !post.getUserId().equals(currentUserId) && !SecurityUtils.isAdminOrModerator()) {
            throw BusinessException.of(ResultCode.POST_NOT_FOUND, "帖子不存在");
        }

        // Redis 去重浏览量（同IP 1小时内只计一次）
        String viewKey = String.format(KEY_VIEW_DEDUP, id, clientIp);
        if (!redisUtils.hasKey(viewKey)) {
            baseMapper.incrementViewCount(id);
            redisUtils.set(viewKey, "1", viewDedupHours, TimeUnit.HOURS);
            post.setViewCount(post.getViewCount() + 1);
        }

        PostDetailVO vo = new PostDetailVO();
        BeanUtils.copyProperties(post, vo);

        // 判断当前用户是否已点赞（未登录返回null）
        Long userId = currentUserId;
        if (userId != null) {
            boolean liked = postLikeMapper.exists(
                    new LambdaQueryWrapper<PostLike>()
                            .eq(PostLike::getUserId, userId)
                            .eq(PostLike::getPostId, id)
            );
            vo.setIsLikedByCurrentUser(liked);
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostVO createPost(PostCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setType(request.getType());
        post.setTags(request.getTags());
        post.setAuditStatus("PENDING");
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setIsPinned(0);
        save(post);

        log.info("帖子创建成功: id={}, userId={}, title={}", post.getId(), userId, post.getTitle());
        return toVO(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostVO updatePost(Long id, PostUpdateRequest request) {
        Post post = getById(id);
        if (post == null) {
            throw BusinessException.of(ResultCode.POST_NOT_FOUND, "帖子不存在");
        }
        checkEditPermission(post);

        if (request.getTitle() != null)   post.setTitle(request.getTitle());
        if (request.getContent() != null) post.setContent(request.getContent());
        if (request.getType() != null)    post.setType(request.getType());
        if (request.getTags() != null)    post.setTags(request.getTags());
        // 普通用户编辑已审核内容后必须重新审核，避免审核后替换成违规内容。
        if (!SecurityUtils.isAdminOrModerator()) {
            post.setAuditStatus("PENDING");
            post.setAuditRemark(null);
        }
        updateById(post);
        return toVO(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long id) {
        Post post = getById(id);
        if (post == null) {
            throw BusinessException.of(ResultCode.POST_NOT_FOUND, "帖子不存在");
        }
        checkEditPermission(post);
        removeById(id);
        log.info("帖子删除成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleLike(Long postId) {
        if (getById(postId) == null) {
            throw BusinessException.of(ResultCode.POST_NOT_FOUND, "帖子不存在");
        }
        Long userId = SecurityUtils.getCurrentUserId();

        PostLike existing = postLikeMapper.selectOne(
                new LambdaQueryWrapper<PostLike>()
                        .eq(PostLike::getUserId, userId)
                        .eq(PostLike::getPostId, postId)
        );

        if (existing != null) {
            postLikeMapper.deleteById(existing.getId());
            baseMapper.decrementLikeCount(postId);
        } else {
            PostLike like = new PostLike();
            like.setUserId(userId);
            like.setPostId(postId);
            postLikeMapper.insert(like);
            baseMapper.incrementLikeCount(postId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditPost(Long postId, AuditRequest request) {
        Post post = getById(postId);
        if (post == null) {
            throw BusinessException.of(ResultCode.POST_NOT_FOUND, "帖子不存在");
        }
        if (!"REVIEWING".equals(post.getAuditStatus())) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "请先领取该帖子进入审核中");
        }
        post.setAuditStatus(request.getStatus());
        post.setAuditRemark(request.getRemark());
        updateById(post);
        log.info("帖子审核完成: id={}, status={}", postId, request.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startReview(Long postId) {
        if (getById(postId) == null) {
            throw BusinessException.of(ResultCode.POST_NOT_FOUND, "帖子不存在");
        }
        boolean updated = update(new LambdaUpdateWrapper<Post>()
                .eq(Post::getId, postId)
                .eq(Post::getAuditStatus, "PENDING")
                .set(Post::getAuditStatus, "REVIEWING")
                .set(Post::getAuditRemark, null));
        if (!updated) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "该帖子已被领取或审核完成");
        }
        log.info("帖子进入审核中: id={}, reviewerId={}", postId, SecurityUtils.getCurrentUserId());
    }

    @Override
    public PageVO<PostVO> getMyPosts(String stage, int page, int size) {
        IPage<Post> result = baseMapper.selectWorkflowPage(
                new Page<>(page, size), SecurityUtils.getCurrentUserId(), stage);
        return toPageVO(result);
    }

    @Override
    public PageVO<PostVO> getReviewPosts(String stage, int page, int size) {
        IPage<Post> result = baseMapper.selectWorkflowPage(new Page<>(page, size), null, stage);
        return toPageVO(result);
    }

    @Override
    public PageVO<PostVO> getPostsByUser(Long userId, int page, int size) {
        IPage<Post> result = page(
                new Page<>(page, size),
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getUserId, userId)
                        .eq(Post::getAuditStatus, "APPROVED")
                        .orderByDesc(Post::getCreatedAt)
        );
        return toPageVO(result);
    }

    // ============ 私有工具方法 ============

    /** 校验当前用户是否有权编辑/删除此帖 */
    private void checkEditPermission(Post post) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (!post.getUserId().equals(userId) && !SecurityUtils.isAdminOrModerator()) {
            throw BusinessException.of(ResultCode.POST_ACCESS_DENIED, "无权操作他人帖子");
        }
    }

    private PostVO toVO(Post post) {
        PostVO vo = new PostVO();
        BeanUtils.copyProperties(post, vo);
        return vo;
    }

    private PageVO<PostVO> toPageVO(IPage<Post> page) {
        List<PostVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageVO<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getPages(), records);
    }
}
