package com.company.opl.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.company.opl.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("issue_operation_log")
@EqualsAndHashCode(callSuper = true)
public class IssueOperationLog extends BaseEntity {

    @TableId
    private Long id;
    private Long issueId;
    private String operationType;
    private String fromValue;
    private String toValue;
    private String remark;
    private Long operatorId;
    private String operatorName;
}

