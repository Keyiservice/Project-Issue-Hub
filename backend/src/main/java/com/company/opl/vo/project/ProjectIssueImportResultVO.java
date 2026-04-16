package com.company.opl.vo.project;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProjectIssueImportResultVO {
    private Integer totalCount;
    private Integer addedCount;
    private Integer updatedCount;
    private Integer skippedCount;
    private Integer failedCount;
    private List<String> failedMessages;
}
