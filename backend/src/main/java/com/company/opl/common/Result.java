package com.company.opl.common;

import lombok.Builder;
import lombok.Data;
import org.slf4j.MDC;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Data
@Builder
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    private String traceId;
    private String timestamp;

    public static <T> Result<T> success(T data) {
        return build(ResultCode.SUCCESS, data, ResultCode.SUCCESS.getMessage());
    }

    public static <T> Result<T> success(String message, T data) {
        return build(ResultCode.SUCCESS, data, message);
    }

    public static <T> Result<T> error(ResultCode resultCode, String message) {
        return build(resultCode, null, message);
    }

    public static <T> Result<T> error(ResultCode resultCode) {
        return build(resultCode, null, resultCode.getMessage());
    }

    private static <T> Result<T> build(ResultCode resultCode, T data, String message) {
        return Result.<T>builder()
                .code(resultCode.getCode())
                .message(message)
                .data(data)
                .traceId(resolveTraceId())
                .timestamp(OffsetDateTime.now(ZoneId.of("Asia/Shanghai")).toString())
                .build();
    }

    private static String resolveTraceId() {
        String traceId = MDC.get("traceId");
        return traceId != null ? traceId : UUID.randomUUID().toString().replace("-", "");
    }
}

