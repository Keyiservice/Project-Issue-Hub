package com.company.opl.controller;

import com.company.opl.common.PageResult;
import com.company.opl.common.Result;
import com.company.opl.dto.project.ProjectCreateDTO;
import com.company.opl.dto.project.ProjectUpdateDTO;
import com.company.opl.query.issue.IssueQuery;
import com.company.opl.query.project.ProjectQuery;
import com.company.opl.service.ProjectService;
import com.company.opl.vo.project.ProjectIssueImportResultVO;
import com.company.opl.vo.project.ProjectIssueSummaryVO;
import com.company.opl.vo.project.ProjectVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SITE_USER','RESP_ENGINEER','PROJECT_MANAGER','MANAGEMENT','ADMIN')")
    @Operation(summary = "项目分页查询")
    public Result<PageResult<ProjectVO>> page(ProjectQuery query) {
        return Result.success(projectService.page(query));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('SITE_USER','RESP_ENGINEER','PROJECT_MANAGER','MANAGEMENT','ADMIN')")
    @Operation(summary = "项目全量列表")
    public Result<List<ProjectVO>> listAll() {
        return Result.success(projectService.listAll());
    }

    @GetMapping("/{projectId}/issue-summary")
    @PreAuthorize("hasAnyRole('SITE_USER','RESP_ENGINEER','PROJECT_MANAGER','MANAGEMENT','ADMIN')")
    @Operation(summary = "项目问题汇总")
    public Result<ProjectIssueSummaryVO> issueSummary(@PathVariable Long projectId) {
        return Result.success(projectService.getIssueSummary(projectId));
    }

    @GetMapping("/{projectId}/issue-report/export")
    @PreAuthorize("hasAnyRole('SITE_USER','RESP_ENGINEER','PROJECT_MANAGER','MANAGEMENT','ADMIN')")
    @Operation(summary = "导出项目问题报告")
    public ResponseEntity<byte[]> exportIssueReport(@PathVariable Long projectId, IssueQuery query) {
        byte[] content = projectService.exportIssueReport(projectId, query);
        String fileName = "project-issue-report-" + projectId + "-" + DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now()) + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }

    @PostMapping(path = "/{projectId}/issue-report/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SITE_USER','RESP_ENGINEER','PROJECT_MANAGER','ADMIN')")
    @Operation(summary = "导入项目问题报告")
    public Result<ProjectIssueImportResultVO> importIssueReport(@PathVariable Long projectId,
                                                                @RequestParam("file") MultipartFile file) {
        return Result.success(projectService.importIssueReport(projectId, file));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER','ADMIN')")
    @Operation(summary = "创建项目")
    public Result<Long> create(@Valid @RequestBody ProjectCreateDTO dto) {
        return Result.success(projectService.create(dto));
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER','ADMIN')")
    @Operation(summary = "更新项目")
    public Result<Void> update(@PathVariable Long projectId, @Valid @RequestBody ProjectUpdateDTO dto) {
        projectService.update(projectId, dto);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER','ADMIN')")
    @Operation(summary = "删除项目")
    public Result<Void> delete(@PathVariable Long projectId) {
        projectService.delete(projectId);
        return Result.success("删除成功", null);
    }
}
