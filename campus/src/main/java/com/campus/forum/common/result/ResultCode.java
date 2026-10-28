package com.campus.forum.common.result;

/**
 * 业务状态码常量
 * 规范：2xx 成功；4xx 客户端错误；5xx 服务端错误
 * 业务码：4xxxx 对应各模块错误
 */
public interface ResultCode {

    // -------- 通用 --------
    int SUCCESS        = 200;
    int BAD_REQUEST    = 400;
    int UNAUTHORIZED   = 401;
    int FORBIDDEN      = 403;
    int NOT_FOUND      = 404;
    int INTERNAL_ERROR = 500;
    int TOO_MANY_REQUEST = 429;

    // -------- 用户模块 40001 ~ 40099 --------
    int USERNAME_EXISTS  = 40001;
    int EMAIL_EXISTS     = 40002;
    int USER_NOT_FOUND   = 40003;
    int WRONG_PASSWORD   = 40004;
    int USER_DISABLED    = 40005;
    int TOKEN_EXPIRED    = 40006;
    int TOKEN_INVALID    = 40007;

    // -------- 帖子模块 40101 ~ 40199 --------
    int POST_NOT_FOUND     = 40101;
    int POST_ACCESS_DENIED = 40102;
    int POST_ALREADY_LIKED = 40103;

    // -------- 评论模块 40201 ~ 40299 --------
    int COMMENT_NOT_FOUND     = 40201;
    int COMMENT_ACCESS_DENIED = 40202;

    // -------- 举报模块 40301 ~ 40399 --------
    int REPORT_NOT_FOUND         = 40301;
    int REPORT_DUPLICATE         = 40302;
    int REPORT_TARGET_NOT_FOUND  = 40303;
}
