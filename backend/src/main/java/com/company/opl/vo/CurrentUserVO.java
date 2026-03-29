package com.company.opl.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CurrentUserVO {
    private Long userId;
    private String username;
    private String realName;
    private String departmentCode;
    private String departmentName;
    private Boolean wechatBound;
    private Boolean passwordChangeRequired;
    private List<String> roles;
}
