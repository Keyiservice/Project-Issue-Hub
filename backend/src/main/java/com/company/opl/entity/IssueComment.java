package com.company.opl.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.company.opl.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("issue_comment")
@EqualsAndHashCode(callSuper = true)
public class IssueComment extends BaseEntity {

    @TableId
    private Long id;
    private Long issueId;
    private String commentType;
    private String content;
    private String statusSnapshot;
    private Long operatorId;
    private String operatorName;
}

