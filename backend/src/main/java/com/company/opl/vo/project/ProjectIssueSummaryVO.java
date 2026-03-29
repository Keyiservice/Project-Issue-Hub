package com.company.opl.vo.project;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectIssueSummaryVO {
    private Integer totalIssues;
    private Integer openIssues;
    private Integer closedIssues;
    private Integer highPriorityIssues;
    private Integer unassignedIssues;
    private Integer processingIssues;
    private Integer overdueIssues;
}
