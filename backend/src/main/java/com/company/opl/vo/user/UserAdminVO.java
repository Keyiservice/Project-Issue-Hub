package com.company.opl.vo.user;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserAdminVO {
    private Long id;
    private String userNo;
    private String username;
    private String realName;
    private String mobile;
    private String email;
    private String departmentCode;
    private String departmentName;
    private String status;
    private Boolean wechatBound;
    private String wechatOpenid;
    private LocalDateTime wechatBoundAt;
    private Boolean passwordChangeRequired;
    private LocalDateTime passwordChangedAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private Integer projectCount;
    private List<String> roleCodes;
    private List<String> roleNames;
}
