package com.company.opl.query.issue;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class IssueQuery {
    private Long current = 1L;
    private Long size = 20L;
    private Long projectId;
    private Long reporterId;
    private Long ownerId;
    private String keyword;
    private List<String> statusList;
    private String priority;
    private Boolean overdueOnly;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdTo;
}
