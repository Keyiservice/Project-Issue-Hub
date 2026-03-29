package com.company.opl.vo.user;

import lombok.Data;

@Data
public class UserProjectParticipationVO {
    private Long projectId;
    private String projectNo;
    private String projectName;
    private String customerName;
    private String projectStatus;
    private String projectManagerName;
    private String projectRoleCode;
    private String projectRoleName;
    private Boolean projectManager;
    private Boolean canAssignIssue;
    private Boolean canVerifyIssue;
    private Boolean canCloseIssue;
    private String remark;
}
