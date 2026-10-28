package com.campus.forum.service;

import com.campus.forum.common.result.PageVO;
import com.campus.forum.dto.CommentCreateRequest;
import com.campus.forum.vo.CommentVO;

public interface CommentService {
    PageVO<CommentVO> listComments(Long postId, int page, int size);
    CommentVO createComment(Long postId, CommentCreateRequest request);
    void deleteComment(Long commentId);
    void toggleLike(Long commentId);
}
