package com.company.opl.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectCreateDTO {
    @NotBlank(message = "项目编号不能为空")
    private String projectNo;
    @NotBlank(message = "项目名称不能为空")
    private String projectName;
    @NotBlank(message = "客户名称不能为空")
    private String customerName;
    @NotNull(message = "项目经理不能为空")
    private Long projectManagerId;
    @NotBlank(message = "项目经理名称不能为空")
    private String projectManagerName;
    @NotBlank(message = "项目状态不能为空")
    private String status;
    private LocalDate startDate;
    private LocalDate plannedEndDate;
    private String description;
}

