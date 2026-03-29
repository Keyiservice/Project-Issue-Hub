package com.company.opl.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum IssueStatusEnum {
    NEW("NEW", "新建"),
    ACCEPTED("ACCEPTED", "已受理"),
    IN_PROGRESS("IN_PROGRESS", "处理中"),
    PENDING_VERIFY("PENDING_VERIFY", "待验证"),
    CLOSED("CLOSED", "已关闭"),
    ON_HOLD("ON_HOLD", "已挂起"),
    CANCELED("CANCELED", "已取消");

    private final String code;
    private final String description;

    IssueStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static IssueStatusEnum fromCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知状态: " + code));
    }
}

