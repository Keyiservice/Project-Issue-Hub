package com.company.opl.vo.issue;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class IssueDetailVO {
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
    private String issueFunctionCode;
    private String categoryCode;
    private String sourceCode;
    private String priority;
    private String status;
    private String impactLevel;
    private Boolean affectShipment;
    private Boolean affectCommissioning;
    private Boolean needShutdown;
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
    private LocalDateTime lastFollowUpAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String holdReason;
    private String closeReason;
    private String closeEvidence;
    private Boolean overdue;
    private List<IssueAttachmentVO> attachments;
    private List<IssueCommentVO> comments;
}
