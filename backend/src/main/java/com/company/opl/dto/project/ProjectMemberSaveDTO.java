package com.company.opl.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProjectMemberSaveDTO {

    @NotNull(message = "成员不能为空")
    private Long userId;

    @NotBlank(message = "项目岗位不能为空")
    private String projectRoleCode;

    private Boolean projectManager = Boolean.FALSE;
    private Boolean canAssignIssue = Boolean.FALSE;
    private Boolean canVerifyIssue = Boolean.FALSE;
    private Boolean canCloseIssue = Boolean.FALSE;
    private Integer sortNo = 0;
    private String remark;
}
