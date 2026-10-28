package com.campus.forum.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 统一 API 响应封装
 * 约定：所有接口均返回此对象，前端通过 code == 200 判断成功
 */
@Getter
@Schema(description = "统一响应结果")
public class Result<T> {

    @Schema(description = "状态码，200 表示成功")
    private final int code;

    @Schema(description = "提示消息")
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "响应数据")
    private final T data;

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS, "操作成功", data);
    }

    public static Result<Void> success() {
        return new Result<>(ResultCode.SUCCESS, "操作成功", null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    public boolean isSuccess() {
        return this.code == ResultCode.SUCCESS;
    }
}
