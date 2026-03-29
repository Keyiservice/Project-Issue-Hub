package com.company.opl.service;

import com.company.opl.dto.project.ProjectMemberSaveDTO;
import com.company.opl.dto.project.ProjectMemberBatchImportDTO;
import com.company.opl.vo.project.ProjectMemberRoleOptionVO;
import com.company.opl.vo.project.ProjectMemberImportResultVO;
import com.company.opl.vo.project.ProjectMemberVO;

import java.util.List;

public interface ProjectMemberService {

    List<ProjectMemberVO> listByProjectId(Long projectId);

    List<ProjectMemberRoleOptionVO> listRoleOptions();

    ProjectMemberVO create(Long projectId, ProjectMemberSaveDTO dto);

    ProjectMemberVO update(Long projectId, Long memberId, ProjectMemberSaveDTO dto);

    void remove(Long projectId, Long memberId);

    ProjectMemberImportResultVO importMembers(Long projectId, ProjectMemberBatchImportDTO dto);

    void ensureProjectMember(Long projectId, Long userId);

    void setProjectManager(Long projectId, Long userId);

    ProjectMemberImportResultVO syncMembersFromIssueHistory(Long projectId);
}
