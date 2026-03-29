package com.company.opl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.opl.common.PageResult;
import com.company.opl.dto.project.ProjectCreateDTO;
import com.company.opl.dto.project.ProjectUpdateDTO;
import com.company.opl.entity.Issue;
import com.company.opl.entity.IssueAttachment;
import com.company.opl.entity.Project;
import com.company.opl.entity.ProjectMember;
import com.company.opl.entity.SysUser;
import com.company.opl.enums.IssuePriorityEnum;
import com.company.opl.enums.IssueStatusEnum;
import com.company.opl.enums.UserStatusEnum;
import com.company.opl.exception.BusinessException;
import com.company.opl.mapper.IssueAttachmentMapper;
import com.company.opl.mapper.IssueMapper;
import com.company.opl.mapper.ProjectMapper;
import com.company.opl.mapper.ProjectMemberMapper;
import com.company.opl.mapper.SysUserMapper;
import com.company.opl.query.issue.IssueQuery;
import com.company.opl.query.project.ProjectQuery;
import com.company.opl.service.ProjectMemberService;
import com.company.opl.service.ProjectService;
import com.company.opl.util.ProjectIssueReportExcelExporter;
import com.company.opl.vo.project.ProjectIssueSummaryVO;
import com.company.opl.vo.project.ProjectVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final SysUserMapper sysUserMapper;
    private final ProjectMemberService projectMemberService;
    private final IssueMapper issueMapper;
    private final IssueAttachmentMapper issueAttachmentMapper;
    private final String reportPublicBaseUrl;

    public ProjectServiceImpl(ProjectMapper projectMapper,
                              ProjectMemberMapper projectMemberMapper,
                              SysUserMapper sysUserMapper,
                              ProjectMemberService projectMemberService,
                              IssueMapper issueMapper,
                              IssueAttachmentMapper issueAttachmentMapper,
                              @Value("${report.public-base-url:http://localhost}") String reportPublicBaseUrl) {
        this.projectMapper = projectMapper;
        this.projectMemberMapper = projectMemberMapper;
        this.sysUserMapper = sysUserMapper;
        this.projectMemberService = projectMemberService;
        this.issueMapper = issueMapper;
        this.issueAttachmentMapper = issueAttachmentMapper;
        this.reportPublicBaseUrl = reportPublicBaseUrl;
    }

    @Override
    public Long create(ProjectCreateDTO dto) {
        Project exists = projectMapper.selectOne(new LambdaQueryWrapper<Project>()
                .eq(Project::getProjectNo, dto.getProjectNo())
                .last("limit 1"));
        if (exists != null) {
            throw new BusinessException("项目编号已存在");
        }

        SysUser manager = loadEnabledUser(dto.getProjectManagerId());
        Project project = new Project();
        BeanUtils.copyProperties(dto, project);
        project.setProjectManagerName(manager.getRealName());
        projectMapper.insert(project);
        projectMemberService.setProjectManager(project.getId(), manager.getId());
        return project.getId();
    }

    @Override
    public void update(Long projectId, ProjectUpdateDTO dto) {
        Project project = ensureProjectExists(projectId);
        SysUser manager = loadEnabledUser(dto.getProjectManagerId());

        BeanUtils.copyProperties(dto, project);
        project.setProjectManagerName(manager.getRealName());
        projectMapper.updateById(project);
        projectMemberService.setProjectManager(projectId, manager.getId());
    }

    @Override
    public void delete(Long projectId) {
        ensureProjectExists(projectId);

        long issueCount = issueMapper.selectCount(new LambdaQueryWrapper<Issue>()
                .eq(Issue::getProjectId, projectId));
        if (issueCount > 0) {
            throw new BusinessException("项目下已有问题单，不能直接删除");
        }

        projectMemberMapper.delete(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId));
        projectMapper.deleteById(projectId);
    }

    @Override
    public PageResult<ProjectVO> page(ProjectQuery query) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<Project>()
                .eq(StringUtils.hasText(query.getStatus()), Project::getStatus, query.getStatus())
                .and(StringUtils.hasText(query.getKeyword()), q -> q.like(Project::getProjectNo, query.getKeyword())
                        .or()
                        .like(Project::getProjectName, query.getKeyword())
                        .or()
                        .like(Project::getCustomerName, query.getKeyword()))
                .orderByDesc(Project::getCreatedAt);

        Page<Project> page = projectMapper.selectPage(new Page<>(query.getCurrent(), query.getSize()), wrapper);
        Map<Long, Integer> teamSizeMap = buildTeamSizeMap(page.getRecords());
        List<ProjectVO> records = page.getRecords().stream()
                .map(item -> toVO(item, teamSizeMap))
                .toList();
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    @Override
    public List<ProjectVO> listAll() {
        List<Project> projects = projectMapper.selectList(new LambdaQueryWrapper<Project>().orderByDesc(Project::getCreatedAt));
        Map<Long, Integer> teamSizeMap = buildTeamSizeMap(projects);
        return projects.stream()
                .map(item -> toVO(item, teamSizeMap))
                .toList();
    }

    @Override
    public ProjectIssueSummaryVO getIssueSummary(Long projectId) {
        ensureProjectExists(projectId);
        int totalIssues = Math.toIntExact(issueMapper.selectCount(new LambdaQueryWrapper<Issue>()
                .eq(Issue::getProjectId, projectId)));
        int openIssues = Math.toIntExact(issueMapper.selectCount(activeIssueWrapper(projectId)));
        int closedIssues = Math.toIntExact(issueMapper.selectCount(new LambdaQueryWrapper<Issue>()
                .eq(Issue::getProjectId, projectId)
                .eq(Issue::getStatus, IssueStatusEnum.CLOSED.getCode())));
        int highPriorityIssues = Math.toIntExact(issueMapper.selectCount(activeIssueWrapper(projectId)
                .in(Issue::getPriority, List.of(IssuePriorityEnum.HIGH.name(), IssuePriorityEnum.CRITICAL.name()))));
        int unassignedIssues = Math.toIntExact(issueMapper.selectCount(activeIssueWrapper(projectId)
                .and(q -> q.isNull(Issue::getOwnerId).or().eq(Issue::getOwnerName, ""))));
        int processingIssues = Math.toIntExact(issueMapper.selectCount(new LambdaQueryWrapper<Issue>()
                .eq(Issue::getProjectId, projectId)
                .in(Issue::getStatus, List.of(IssueStatusEnum.IN_PROGRESS.getCode(), IssueStatusEnum.PENDING_VERIFY.getCode()))));
        int overdueIssues = Math.toIntExact(issueMapper.selectCount(activeIssueWrapper(projectId)
                .lt(Issue::getDueAt, LocalDateTime.now())));

        return ProjectIssueSummaryVO.builder()
                .totalIssues(totalIssues)
                .openIssues(openIssues)
                .closedIssues(closedIssues)
                .highPriorityIssues(highPriorityIssues)
                .unassignedIssues(unassignedIssues)
                .processingIssues(processingIssues)
                .overdueIssues(overdueIssues)
                .build();
    }

    @Override
    public byte[] exportIssueReport(Long projectId, IssueQuery query) {
        Project project = ensureProjectExists(projectId);
        IssueQuery exportQuery = normalizeIssueQuery(projectId, query);
        ProjectIssueSummaryVO summary = getIssueSummary(projectId);
        List<Issue> issues = issueMapper.selectList(buildIssueExportWrapper(exportQuery));
        Map<Long, List<IssueAttachment>> attachmentMap = buildAttachmentMap(issues);
        String ownerFilterLabel = resolveOwnerFilterLabel(exportQuery.getOwnerId());
        return ProjectIssueReportExcelExporter.export(project, summary, issues, exportQuery, ownerFilterLabel, attachmentMap, reportPublicBaseUrl);
    }

    private Map<Long, List<IssueAttachment>> buildAttachmentMap(List<Issue> issues) {
        if (issues == null || issues.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> issueIds = issues.stream().map(Issue::getId).toList();
        return issueAttachmentMapper.selectList(new LambdaQueryWrapper<IssueAttachment>()
                        .in(IssueAttachment::getIssueId, issueIds)
                        .orderByAsc(IssueAttachment::getCreatedAt))
                .stream()
                .collect(Collectors.groupingBy(IssueAttachment::getIssueId, LinkedHashMap::new, Collectors.toList()));
    }

    private Map<Long, Integer> buildTeamSizeMap(List<Project> projects) {
        if (projects == null || projects.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> projectIds = projects.stream().map(Project::getId).toList();
        Map<Long, Integer> counters = new LinkedHashMap<>();
        projectMemberMapper.selectList(new LambdaQueryWrapper<ProjectMember>()
                        .in(ProjectMember::getProjectId, projectIds))
                .forEach(item -> counters.merge(item.getProjectId(), 1, Integer::sum));
        return counters;
    }

    private ProjectVO toVO(Project project, Map<Long, Integer> teamSizeMap) {
        ProjectVO vo = new ProjectVO();
        BeanUtils.copyProperties(project, vo);
        vo.setTeamSize(teamSizeMap.getOrDefault(project.getId(), 0));
        return vo;
    }

    private LambdaQueryWrapper<Issue> activeIssueWrapper(Long projectId) {
        return new LambdaQueryWrapper<Issue>()
                .eq(Issue::getProjectId, projectId)
                .notIn(Issue::getStatus, List.of(IssueStatusEnum.CLOSED.getCode(), IssueStatusEnum.CANCELED.getCode()));
    }

    private LambdaQueryWrapper<Issue> buildIssueExportWrapper(IssueQuery query) {
        LambdaQueryWrapper<Issue> wrapper = new LambdaQueryWrapper<Issue>()
                .eq(Issue::getProjectId, query.getProjectId())
                .eq(query.getReporterId() != null, Issue::getReporterId, query.getReporterId())
                .eq(query.getOwnerId() != null, Issue::getOwnerId, query.getOwnerId())
                .eq(StringUtils.hasText(query.getPriority()), Issue::getPriority, query.getPriority())
                .ge(query.getCreatedFrom() != null, Issue::getCreatedAt, query.getCreatedFrom())
                .le(query.getCreatedTo() != null, Issue::getCreatedAt, query.getCreatedTo())
                .and(StringUtils.hasText(query.getKeyword()), q -> q
                        .like(Issue::getTitle, query.getKeyword())
                        .or()
                        .like(Issue::getIssueNo, query.getKeyword())
                        .or()
                        .like(Issue::getOplNo, query.getKeyword())
                        .or()
                        .like(Issue::getDescription, query.getKeyword()))
                .in(query.getStatusList() != null && !query.getStatusList().isEmpty(), Issue::getStatus, query.getStatusList())
                .orderByDesc(Issue::getCreatedAt);

        if (Boolean.TRUE.equals(query.getOverdueOnly())) {
            wrapper.lt(Issue::getDueAt, LocalDateTime.now())
                    .ne(Issue::getStatus, IssueStatusEnum.CLOSED.getCode())
                    .ne(Issue::getStatus, IssueStatusEnum.CANCELED.getCode());
        }
        return wrapper;
    }

    private IssueQuery normalizeIssueQuery(Long projectId, IssueQuery query) {
        IssueQuery exportQuery = new IssueQuery();
        exportQuery.setProjectId(projectId);
        if (query == null) {
            return exportQuery;
        }
        exportQuery.setReporterId(query.getReporterId());
        exportQuery.setOwnerId(query.getOwnerId());
        exportQuery.setKeyword(query.getKeyword());
        exportQuery.setStatusList(query.getStatusList());
        exportQuery.setPriority(query.getPriority());
        exportQuery.setOverdueOnly(query.getOverdueOnly());
        exportQuery.setCreatedFrom(query.getCreatedFrom());
        exportQuery.setCreatedTo(query.getCreatedTo());
        return exportQuery;
    }

    private String resolveOwnerFilterLabel(Long ownerId) {
        if (ownerId == null) {
            return "全部";
        }
        SysUser user = sysUserMapper.selectById(ownerId);
        return user == null ? String.valueOf(ownerId) : user.getRealName();
    }

    private SysUser loadEnabledUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !UserStatusEnum.ENABLED.name().equals(user.getStatus())) {
            throw new BusinessException("项目经理不存在或未启用");
        }
        return user;
    }

    private Project ensureProjectExists(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        return project;
    }
}
