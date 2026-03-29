package com.company.opl.controller;

import com.company.opl.common.Result;
import com.company.opl.dto.project.ProjectMemberBatchImportDTO;
import com.company.opl.dto.project.ProjectMemberSaveDTO;
import com.company.opl.service.ProjectMemberService;
import com.company.opl.vo.project.ProjectMemberImportResultVO;
import com.company.opl.vo.project.ProjectMemberRoleOptionVO;
import com.company.opl.vo.project.ProjectMemberVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    public ProjectMemberController(ProjectMemberService projectMemberService) {
        this.projectMemberService = projectMemberService;
    }

    @GetMapping("/member-role-options")
    @PreAuthorize("hasAnyRole('SITE_USER','RESP_ENGINEER','PROJECT_MANAGER','MANAGEMENT','ADMIN')")
    @Operation(summary = "项目岗位选项")
    public Result<List<ProjectMemberRoleOptionVO>> roleOptions() {
        return Result.success(projectMemberService.listRoleOptions());
    }

    @GetMapping("/{projectId}/members")
    @PreAuthorize("hasAnyRole('SITE_USER','RESP_ENGINEER','PROJECT_MANAGER','MANAGEMENT','ADMIN')")
    @Operation(summary = "项目成员列表")
    public Result<List<ProjectMemberVO>> list(@PathVariable Long projectId) {
        return Result.success(projectMemberService.listByProjectId(projectId));
    }

    @PostMapping("/{projectId}/members")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER','ADMIN')")
    @Operation(summary = "新增项目成员")
    public Result<ProjectMemberVO> create(@PathVariable Long projectId,
                                          @Valid @RequestBody ProjectMemberSaveDTO dto) {
        return Result.success(projectMemberService.create(projectId, dto));
    }

    @PostMapping("/{projectId}/members/import")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER','ADMIN')")
    @Operation(summary = "批量导入项目成员")
    public Result<ProjectMemberImportResultVO> importMembers(@PathVariable Long projectId,
                                                             @Valid @RequestBody ProjectMemberBatchImportDTO dto) {
        return Result.success(projectMemberService.importMembers(projectId, dto));
    }

    @PostMapping("/{projectId}/members/sync-from-issues")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER','ADMIN')")
    @Operation(summary = "从项目现有 OPL 同步成员")
    public Result<ProjectMemberImportResultVO> syncFromIssues(@PathVariable Long projectId) {
        return Result.success(projectMemberService.syncMembersFromIssueHistory(projectId));
    }

    @PutMapping("/{projectId}/members/{memberId}")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER','ADMIN')")
    @Operation(summary = "更新项目成员")
    public Result<ProjectMemberVO> update(@PathVariable Long projectId,
                                          @PathVariable Long memberId,
                                          @Valid @RequestBody ProjectMemberSaveDTO dto) {
        return Result.success(projectMemberService.update(projectId, memberId, dto));
    }

    @DeleteMapping("/{projectId}/members/{memberId}")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER','ADMIN')")
    @Operation(summary = "移除项目成员")
    public Result<Void> remove(@PathVariable Long projectId, @PathVariable Long memberId) {
        projectMemberService.remove(projectId, memberId);
        return Result.success(null);
    }
}
