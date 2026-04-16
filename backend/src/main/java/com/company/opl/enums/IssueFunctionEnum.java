package com.company.opl.enums;

import java.util.Arrays;

public enum IssueFunctionEnum {
    PAT("PAT"),
    FAT("FAT"),
    DESIGN("设计"),
    SAFETY("安全"),
    LOGISTICS("物流"),
    PROCUREMENT("采购"),
    ASSEMBLY("装配");

    private final String description;

    IssueFunctionEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static boolean contains(String code) {
        return code != null && Arrays.stream(values()).anyMatch(item -> item.name().equals(code));
    }
}
