package com.company.opl.enums;

import lombok.Getter;

@Getter
public enum IssueOperationTypeEnum {
    CREATE("CREATE"),
    ASSIGN("ASSIGN"),
    STATUS_CHANGE("STATUS_CHANGE"),
    PRIORITY_CHANGE("PRIORITY_CHANGE"),
    ATTACHMENT_APPEND("ATTACHMENT_APPEND"),
    ATTACHMENT_DELETE("ATTACHMENT_DELETE"),
    COMMENT("COMMENT"),
    CLOSE("CLOSE"),
    DELETE("DELETE");

    private final String code;

    IssueOperationTypeEnum(String code) {
        this.code = code;
    }
}
