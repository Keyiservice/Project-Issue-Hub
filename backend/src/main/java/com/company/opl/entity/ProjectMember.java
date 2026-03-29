package com.company.opl.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.company.opl.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("project_member")
@EqualsAndHashCode(callSuper = true)
public class ProjectMember extends BaseEntity {

    @TableId
    private Long id;
    private Long projectId;
    private Long userId;
    private String projectRoleCode;
    private String projectRoleName;
    private Integer isProjectManager;
    private Integer canAssignIssue;
    private Integer canVerifyIssue;
    private Integer canCloseIssue;
    private Integer sortNo;
    private String remark;
}
