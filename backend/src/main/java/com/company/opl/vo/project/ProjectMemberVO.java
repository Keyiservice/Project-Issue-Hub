package com.company.opl.vo.project;

import lombok.Data;

@Data
public class ProjectMemberVO {
    private Long id;
    private Long projectId;
    private Long userId;
    private String username;
    private String realName;
    private String mobile;
    private String departmentCode;
    private String departmentName;
    private String projectRoleCode;
    private String projectRoleName;
    private Boolean projectManager;
    private Boolean canAssignIssue;
    private Boolean canVerifyIssue;
    private Boolean canCloseIssue;
    private Integer sortNo;
    private String remark;
}
