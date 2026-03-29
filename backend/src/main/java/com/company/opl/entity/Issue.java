package com.company.opl.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.company.opl.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("issue")
@EqualsAndHashCode(callSuper = true)
public class Issue extends BaseEntity {

    @TableId
    private Long id;
    private String issueNo;
    private String title;
    private String description;
    private String oplNo;
    private String actionPlan;
    private String pilotName;
    private String legacyStatus;
    private String legacyComment;
    private LocalDate finishDate;
    private String finishEvidence;
    private Long projectId;
    private String projectName;
    private String deviceName;
    private String moduleName;
    private String categoryCode;
    private String sourceCode;
    private String priority;
    private String status;
    private String impactLevel;
    private Integer affectShipment;
    private Integer affectCommissioning;
    private Integer needShutdown;
    private Long reporterId;
    private String reporterName;
    private Long ownerId;
    private String ownerName;
    private String ownerDepartmentCode;
    private String ownerDepartmentName;
    private LocalDateTime occurredAt;
    private LocalDateTime dueAt;
    private LocalDateTime closedAt;
    private Long closedById;
    private String closedByName;
    private String holdReason;
    private String closeReason;
    private String closeEvidence;
    private Long currentHandlerId;
    private String currentHandlerName;
    private LocalDateTime lastFollowUpAt;
    private Integer version;
}
