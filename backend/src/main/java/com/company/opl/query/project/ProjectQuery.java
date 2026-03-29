package com.company.opl.query.project;

import lombok.Data;

@Data
public class ProjectQuery {
    private Long current = 1L;
    private Long size = 20L;
    private String keyword;
    private String status;
}

