package com.campus.forum.service;

import com.campus.forum.common.result.PageVO;
import com.campus.forum.dto.AuditRequest;
import com.campus.forum.dto.PostCreateRequest;
import com.campus.forum.dto.PostPageRequest;
import com.campus.forum.dto.PostUpdateRequest;
import com.campus.forum.vo.PostDetailVO;
import com.campus.forum.vo.PostVO;

public interface PostService {
    PageVO<PostVO> listPosts(PostPageRequest request);
    PageVO<PostVO> getHotPosts(int page, int size);
    PostDetailVO getPostDetail(Long id, String clientIp);
    PostVO createPost(PostCreateRequest request);
    PostVO updatePost(Long id, PostUpdateRequest request);
    void deletePost(Long id);
    void toggleLike(Long postId);
    void auditPost(Long postId, AuditRequest request);
    void startReview(Long postId);
    PageVO<PostVO> getMyPosts(String stage, int page, int size);
    PageVO<PostVO> getReviewPosts(String stage, int page, int size);
    PageVO<PostVO> getPostsByUser(Long userId, int page, int size);
}
