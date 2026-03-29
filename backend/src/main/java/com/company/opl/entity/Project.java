package com.company.opl.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.company.opl.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@TableName("project")
@EqualsAndHashCode(callSuper = true)
public class Project extends BaseEntity {

    @TableId
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
}

