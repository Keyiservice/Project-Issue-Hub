package com.company.opl.service;

import com.company.opl.common.PageResult;
import com.company.opl.dto.project.ProjectCreateDTO;
import com.company.opl.dto.project.ProjectUpdateDTO;
import com.company.opl.query.issue.IssueQuery;
import com.company.opl.query.project.ProjectQuery;
import com.company.opl.vo.project.ProjectIssueSummaryVO;
import com.company.opl.vo.project.ProjectVO;

import java.util.List;

public interface ProjectService {

    Long create(ProjectCreateDTO dto);

    void update(Long projectId, ProjectUpdateDTO dto);

    void delete(Long projectId);

    PageResult<ProjectVO> page(ProjectQuery query);

    List<ProjectVO> listAll();

    ProjectIssueSummaryVO getIssueSummary(Long projectId);

    byte[] exportIssueReport(Long projectId, IssueQuery query);
}
