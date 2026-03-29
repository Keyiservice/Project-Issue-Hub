package com.company.opl.common;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(0, "success"),
    VALIDATE_ERROR(400, "参数校验失败"),
    UNAUTHORIZED(401, "未认证或认证已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    BUSINESS_ERROR(409, "业务处理失败"),
    SYSTEM_ERROR(500, "系统异常");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}

