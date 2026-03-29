package com.company.opl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.company.opl.dto.project.ProjectMemberBatchImportDTO;
import com.company.opl.dto.project.ProjectMemberImportItemDTO;
import com.company.opl.dto.project.ProjectMemberSaveDTO;
import com.company.opl.entity.Issue;
import com.company.opl.entity.Project;
import com.company.opl.entity.ProjectMember;
import com.company.opl.entity.SysUser;
import com.company.opl.enums.ProjectMemberRoleEnum;
import com.company.opl.enums.UserStatusEnum;
import com.company.opl.exception.BusinessException;
import com.company.opl.mapper.IssueMapper;
import com.company.opl.mapper.ProjectMapper;
import com.company.opl.mapper.ProjectMemberMapper;
import com.company.opl.mapper.SysUserMapper;
import com.company.opl.service.ProjectMemberService;
import com.company.opl.vo.project.ProjectMemberImportResultVO;
import com.company.opl.vo.project.ProjectMemberRoleOptionVO;
import com.company.opl.vo.project.ProjectMemberVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectMemberMapper projectMemberMapper;
    private final ProjectMapper projectMapper;
    private final SysUserMapper sysUserMapper;
    private final IssueMapper issueMapper;

    public ProjectMemberServiceImpl(ProjectMemberMapper projectMemberMapper,
                                    ProjectMapper projectMapper,
                                    SysUserMapper sysUserMapper,
                                    IssueMapper issueMapper) {
        this.projectMemberMapper = projectMemberMapper;
        this.projectMapper = projectMapper;
        this.sysUserMapper = sysUserMapper;
        this.issueMapper = issueMapper;
    }

    @Override
    public List<ProjectMemberVO> listByProjectId(Long projectId) {
        ensureProjectExists(projectId);
        List<ProjectMember> members = projectMemberMapper.selectList(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .orderByDesc(ProjectMember::getIsProjectManager)
                .orderByAsc(ProjectMember::getSortNo)
                .orderByAsc(ProjectMember::getCreatedAt));
        if (members.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> userIds = members.stream().map(ProjectMember::getUserId).distinct().toList();
        Map<Long, SysUser> userMap = sysUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, item -> item));
        return members.stream().map(member -> toVO(member, userMap.get(member.getUserId()))).toList();
    }

    @Override
    public List<ProjectMemberRoleOptionVO> listRoleOptions() {
        return Arrays.stream(ProjectMemberRoleEnum.values()).map(item -> {
            ProjectMemberRoleOptionVO vo = new ProjectMemberRoleOptionVO();
            vo.setCode(item.getCode());
            vo.setName(item.getLabel());
            return vo;
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectMemberVO create(Long projectId, ProjectMemberSaveDTO dto) {
        ensureProjectExists(projectId);
        ensureUniqueMember(projectId, dto.getUserId(), null);
        SysUser user = loadEnabledUser(dto.getUserId());

        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(user.getId());
        applyMemberPayload(member, dto);
        projectMemberMapper.insert(member);

        if (Boolean.TRUE.equals(dto.getProjectManager())) {
            setProjectManager(projectId, user.getId());
        }
        return toVO(loadMember(projectId, member.getId()), user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectMemberVO update(Long projectId, Long memberId, ProjectMemberSaveDTO dto) {
        ProjectMember member = loadMember(projectId, memberId);
        Project project = ensureProjectExists(projectId);
        if (project.getProjectManagerId() != null
                && project.getProjectManagerId().equals(member.getUserId())
                && !Boolean.TRUE.equals(dto.getProjectManager())) {
            throw new BusinessException("当前项目经理不能直接取消，请先指定新的项目经理");
        }

        ensureUniqueMember(projectId, dto.getUserId(), memberId);
        SysUser user = loadEnabledUser(dto.getUserId());
        member.setUserId(user.getId());
        applyMemberPayload(member, dto);
        projectMemberMapper.updateById(member);

        if (Boolean.TRUE.equals(dto.getProjectManager())) {
            setProjectManager(projectId, user.getId());
        }
        return toVO(loadMember(projectId, memberId), user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long projectId, Long memberId) {
        ProjectMember member = loadMember(projectId, memberId);
        Project project = ensureProjectExists(projectId);
        if (project.getProjectManagerId() != null && project.getProjectManagerId().equals(member.getUserId())) {
            throw new BusinessException("当前项目经理不能直接移除，请先调整项目经理");
        }
        projectMemberMapper.deleteById(memberId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectMemberImportResultVO importMembers(Long projectId, ProjectMemberBatchImportDTO dto) {
        ensureProjectExists(projectId);
        int successCount = 0;
        List<String> failedMessages = new ArrayList<>();

        for (int index = 0; index < dto.getItems().size(); index++) {
            ProjectMemberImportItemDTO item = dto.getItems().get(index);
            try {
                importSingleMember(projectId, item);
                successCount++;
            } catch (Exception ex) {
                failedMessages.add("第 " + (index + 1) + " 行: " + ex.getMessage());
            }
        }

        return ProjectMemberImportResultVO.builder()
                .totalCount(dto.getItems().size())
                .successCount(successCount)
                .failedCount(failedMessages.size())
                .failedMessages(failedMessages)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectMemberImportResultVO syncMembersFromIssueHistory(Long projectId) {
        Project project = ensureProjectExists(projectId);
        List<Issue> issues = issueMapper.selectList(new LambdaQueryWrapper<Issue>()
                .eq(Issue::getProjectId, projectId)
                .orderByAsc(Issue::getId));

        Set<String> candidateNames = new LinkedHashSet<>();
        for (Issue issue : issues) {
            collectName(candidateNames, issue.getOwnerName());
            collectName(candidateNames, issue.getPilotName());
        }

        if (candidateNames.isEmpty()) {
            return ProjectMemberImportResultVO.builder()
                    .totalCount(0)
                    .successCount(0)
                    .failedCount(0)
                    .failedMessages(Collections.emptyList())
                    .build();
        }

        List<SysUser> users = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getStatus, UserStatusEnum.ENABLED.name()));
        Map<String, SysUser> userByRealName = new LinkedHashMap<>();
        for (SysUser user : users) {
            if (StringUtils.hasText(user.getRealName())) {
                userByRealName.putIfAbsent(normalizePersonName(user.getRealName()), user);
            }
        }

        Map<Long, ProjectMember> existingMembers = projectMemberMapper.selectList(new LambdaQueryWrapper<ProjectMember>()
                        .eq(ProjectMember::getProjectId, projectId))
                .stream()
                .collect(Collectors.toMap(ProjectMember::getUserId, item -> item, (left, right) -> left, LinkedHashMap::new));

        int successCount = 0;
        List<String> failedMessages = new ArrayList<>();

        for (String name : candidateNames) {
            SysUser user = userByRealName.get(normalizePersonName(name));
            if (user == null) {
                failedMessages.add("未找到匹配的系统用户: " + name);
                continue;
            }
            if (existingMembers.containsKey(user.getId())) {
                successCount++;
                continue;
            }

            ProjectMember member = new ProjectMember();
            member.setProjectId(projectId);
            member.setUserId(user.getId());
            member.setProjectRoleCode(ProjectMemberRoleEnum.TEAM_MEMBER.getCode());
            member.setProjectRoleName(ProjectMemberRoleEnum.TEAM_MEMBER.getLabel());
            member.setIsProjectManager(project.getProjectManagerId() != null && project.getProjectManagerId().equals(user.getId()) ? 1 : 0);
            member.setCanAssignIssue(0);
            member.setCanVerifyIssue(0);
            member.setCanCloseIssue(0);
            member.setSortNo(100);
            member.setRemark("由历史 OPL 自动同步");
            projectMemberMapper.insert(member);
            existingMembers.put(user.getId(), member);
            successCount++;
        }

        return ProjectMemberImportResultVO.builder()
                .totalCount(candidateNames.size())
                .successCount(successCount)
                .failedCount(failedMessages.size())
                .failedMessages(failedMessages)
                .build();
    }

    @Override
    public void ensureProjectMember(Long projectId, Long userId) {
        long count = projectMemberMapper.selectCount(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId));
        if (count == 0) {
            throw new BusinessException("责任人不在当前项目团队内");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setProjectManager(Long projectId, Long userId) {
        ensureProjectExists(projectId);
        SysUser user = loadEnabledUser(userId);

        projectMemberMapper.update(null, new LambdaUpdateWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .set(ProjectMember::getIsProjectManager, 0));

        ProjectMember member = projectMemberMapper.selectOne(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId)
                .last("limit 1"));
        if (member == null) {
            member = new ProjectMember();
            member.setProjectId(projectId);
            member.setUserId(userId);
            member.setProjectRoleCode(ProjectMemberRoleEnum.PROJECT_MANAGER.getCode());
            member.setProjectRoleName(ProjectMemberRoleEnum.PROJECT_MANAGER.getLabel());
            member.setCanAssignIssue(1);
            member.setCanVerifyIssue(1);
            member.setCanCloseIssue(1);
            member.setIsProjectManager(1);
            member.setSortNo(0);
            projectMemberMapper.insert(member);
        } else {
            if (!StringUtils.hasText(member.getProjectRoleCode())) {
                member.setProjectRoleCode(ProjectMemberRoleEnum.PROJECT_MANAGER.getCode());
                member.setProjectRoleName(ProjectMemberRoleEnum.PROJECT_MANAGER.getLabel());
            }
            member.setIsProjectManager(1);
            member.setCanAssignIssue(1);
            member.setCanVerifyIssue(1);
            member.setCanCloseIssue(1);
            projectMemberMapper.updateById(member);
        }

        Project update = new Project();
        update.setId(projectId);
        update.setProjectManagerId(user.getId());
        update.setProjectManagerName(user.getRealName());
        projectMapper.updateById(update);
    }

    private void applyMemberPayload(ProjectMember member, ProjectMemberSaveDTO dto) {
        ProjectMemberRoleEnum role = ProjectMemberRoleEnum.fromCode(dto.getProjectRoleCode());
        member.setProjectRoleCode(role.getCode());
        member.setProjectRoleName(role.getLabel());
        member.setIsProjectManager(Boolean.TRUE.equals(dto.getProjectManager()) ? 1 : 0);
        member.setCanAssignIssue(Boolean.TRUE.equals(dto.getProjectManager()) || Boolean.TRUE.equals(dto.getCanAssignIssue()) ? 1 : 0);
        member.setCanVerifyIssue(Boolean.TRUE.equals(dto.getProjectManager()) || Boolean.TRUE.equals(dto.getCanVerifyIssue()) ? 1 : 0);
        member.setCanCloseIssue(Boolean.TRUE.equals(dto.getProjectManager()) || Boolean.TRUE.equals(dto.getCanCloseIssue()) ? 1 : 0);
        member.setSortNo(dto.getSortNo() == null ? 0 : dto.getSortNo());
        member.setRemark(StringUtils.hasText(dto.getRemark()) ? dto.getRemark().trim() : null);
    }

    private void importSingleMember(Long projectId, ProjectMemberImportItemDTO item) {
        SysUser user = loadEnabledUserByUsername(item.getUsername());
        ProjectMemberSaveDTO dto = new ProjectMemberSaveDTO();
        dto.setUserId(user.getId());
        dto.setProjectRoleCode(item.getProjectRoleCode());
        dto.setProjectManager(item.getProjectManager());
        dto.setCanAssignIssue(item.getCanAssignIssue());
        dto.setCanVerifyIssue(item.getCanVerifyIssue());
        dto.setCanCloseIssue(item.getCanCloseIssue());
        dto.setSortNo(item.getSortNo());
        dto.setRemark(item.getRemark());

        ProjectMember member = projectMemberMapper.selectOne(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, user.getId())
                .last("limit 1"));
        if (member == null) {
            member = new ProjectMember();
            member.setProjectId(projectId);
            member.setUserId(user.getId());
            applyMemberPayload(member, dto);
            projectMemberMapper.insert(member);
        } else {
            member.setUserId(user.getId());
            applyMemberPayload(member, dto);
            projectMemberMapper.updateById(member);
        }

        if (Boolean.TRUE.equals(item.getProjectManager())) {
            setProjectManager(projectId, user.getId());
        }
    }

    private void ensureUniqueMember(Long projectId, Long userId, Long excludeId) {
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId);
        if (excludeId != null) {
            wrapper.ne(ProjectMember::getId, excludeId);
        }
        long count = projectMemberMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("该成员已在当前项目团队中");
        }
    }

    private Project ensureProjectExists(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        return project;
    }

    private SysUser loadEnabledUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !UserStatusEnum.ENABLED.name().equals(user.getStatus())) {
            throw new BusinessException("用户不存在或未启用");
        }
        return user;
    }

    private SysUser loadEnabledUserByUsername(String username) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username.trim())
                .last("limit 1"));
        if (user == null || !UserStatusEnum.ENABLED.name().equals(user.getStatus())) {
            throw new BusinessException("账号不存在或未启用: " + username);
        }
        return user;
    }

    private ProjectMember loadMember(Long projectId, Long memberId) {
        ProjectMember member = projectMemberMapper.selectOne(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getId, memberId)
                .last("limit 1"));
        if (member == null) {
            throw new BusinessException("项目成员不存在");
        }
        return member;
    }

    private ProjectMemberVO toVO(ProjectMember member, SysUser user) {
        ProjectMemberVO vo = new ProjectMemberVO();
        vo.setId(member.getId());
        vo.setProjectId(member.getProjectId());
        vo.setUserId(member.getUserId());
        vo.setProjectRoleCode(member.getProjectRoleCode());
        vo.setProjectRoleName(member.getProjectRoleName());
        vo.setProjectManager(member.getIsProjectManager() != null && member.getIsProjectManager() == 1);
        vo.setCanAssignIssue(member.getCanAssignIssue() != null && member.getCanAssignIssue() == 1);
        vo.setCanVerifyIssue(member.getCanVerifyIssue() != null && member.getCanVerifyIssue() == 1);
        vo.setCanCloseIssue(member.getCanCloseIssue() != null && member.getCanCloseIssue() == 1);
        vo.setSortNo(member.getSortNo());
        vo.setRemark(member.getRemark());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
            vo.setMobile(user.getMobile());
            vo.setDepartmentCode(user.getDepartmentCode());
            vo.setDepartmentName(user.getDepartmentName());
        }
        return vo;
    }

    private void collectName(Set<String> names, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        String trimmed = value.trim();
        if (!trimmed.contains("/")) {
            names.add(trimmed);
        }
    }

    private String normalizePersonName(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
