package com.company.opl.vo.project;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProjectMemberImportResultVO {
    private Integer totalCount;
    private Integer successCount;
    private Integer failedCount;
    private List<String> failedMessages;
}
