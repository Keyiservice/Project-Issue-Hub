package com.company.opl.query.user;

import lombok.Data;

@Data
public class UserQuery {

    private Long current = 1L;
    private Long size = 10L;
    private String keyword;
    private String status;
    private String departmentCode;
}
