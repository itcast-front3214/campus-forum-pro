package com.campus.forum.common.exception;

import lombok.Getter;

/**
 * 业务异常 —— 可预期的业务错误，统一由 GlobalExceptionHandler 处理
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /** 快捷工厂：直接传 ResultCode 接口常量 */
    public static BusinessException of(int code, String message) {
        return new BusinessException(code, message);
    }
}
