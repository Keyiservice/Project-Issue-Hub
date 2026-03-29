package com.company.opl.vo.project;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProjectVO {
    private Long id;
    private String projectNo;
    private String projectName;
    private String customerName;
    private Long projectManagerId;
    private String projectManagerName;
    private String status;
    private LocalDate startDate;
    private LocalDate plannedEndDate;
    private String description;
    private Integer teamSize;
    private LocalDateTime createdAt;
}
