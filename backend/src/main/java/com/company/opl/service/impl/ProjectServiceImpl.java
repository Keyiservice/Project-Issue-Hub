package com.company.opl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.opl.common.PageResult;
import com.company.opl.dto.project.ProjectCreateDTO;
import com.company.opl.dto.project.ProjectUpdateDTO;
import com.company.opl.entity.Issue;
import com.company.opl.entity.IssueAttachment;
import com.company.opl.entity.IssueOperationLog;
import com.company.opl.entity.Project;
import com.company.opl.entity.ProjectMember;
import com.company.opl.entity.SysUser;
import com.company.opl.enums.IssueOperationTypeEnum;
import com.company.opl.enums.IssuePriorityEnum;
import com.company.opl.enums.IssueStatusEnum;
import com.company.opl.enums.UserStatusEnum;
import com.company.opl.exception.BusinessException;
import com.company.opl.mapper.IssueAttachmentMapper;
import com.company.opl.mapper.IssueMapper;
import com.company.opl.mapper.IssueOperationLogMapper;
import com.company.opl.mapper.ProjectMapper;
import com.company.opl.mapper.ProjectMemberMapper;
import com.company.opl.mapper.SysUserMapper;
import com.company.opl.service.IssueCommentService;
import com.company.opl.query.issue.IssueQuery;
import com.company.opl.query.project.ProjectQuery;
import com.company.opl.service.ProjectMemberService;
import com.company.opl.service.ProjectService;
import com.company.opl.util.IssueNoGenerator;
import com.company.opl.util.ProjectIssueReportExcelExporter;
import com.company.opl.util.SecurityUtils;
import com.company.opl.vo.project.ProjectIssueImportResultVO;
import com.company.opl.vo.project.ProjectIssueSummaryVO;
import com.company.opl.vo.project.ProjectVO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private static final DateTimeFormatter[] DATE_PATTERNS = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };

    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final SysUserMapper sysUserMapper;
    private final ProjectMemberService projectMemberService;
    private final IssueMapper issueMapper;
    private final IssueAttachmentMapper issueAttachmentMapper;
    private final IssueOperationLogMapper issueOperationLogMapper;
    private final IssueCommentService issueCommentService;
    private final String reportPublicBaseUrl;

    public ProjectServiceImpl(ProjectMapper projectMapper,
                              ProjectMemberMapper projectMemberMapper,
                              SysUserMapper sysUserMapper,
                              ProjectMemberService projectMemberService,
                              IssueMapper issueMapper,
                              IssueAttachmentMapper issueAttachmentMapper,
                              IssueOperationLogMapper issueOperationLogMapper,
                              IssueCommentService issueCommentService,
                              @Value("${report.public-base-url:http://localhost}") String reportPublicBaseUrl) {
        this.projectMapper = projectMapper;
        this.projectMemberMapper = projectMemberMapper;
        this.sysUserMapper = sysUserMapper;
        this.projectMemberService = projectMemberService;
        this.issueMapper = issueMapper;
        this.issueAttachmentMapper = issueAttachmentMapper;
        this.issueOperationLogMapper = issueOperationLogMapper;
        this.issueCommentService = issueCommentService;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectIssueImportResultVO importIssueReport(Long projectId, MultipartFile file) {
        Project project = ensureProjectExists(projectId);
        validateImportFile(file);

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            int headerRowIndex = locateHeaderRow(sheet);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            DataFormatter formatter = new DataFormatter(Locale.getDefault());

            Map<String, Issue> existingMap = loadExistingIssueMap(projectId);
            Map<String, SysUser> memberNameMap = loadProjectMemberNameMap(projectId);

            int totalCount = 0;
            int addedCount = 0;
            int updatedCount = 0;
            int skippedCount = 0;
            List<String> failedMessages = new java.util.ArrayList<>();

            for (int rowIndex = headerRowIndex + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                String description = readCellString(row.getCell(2), formatter, evaluator);
                if (!StringUtils.hasText(description)) {
                    skippedCount++;
                    continue;
                }

                totalCount++;
                try {
                    ImportRow importRow = parseImportRow(row, formatter, evaluator);
                    String descriptionKey = normalizeDescriptionKey(importRow.description());
                    Issue matchedIssue = existingMap.get(descriptionKey);
                    boolean created = matchedIssue == null;
                    Issue targetIssue = created ? buildImportedIssue(project, importRow, memberNameMap) : applyImportedIssue(matchedIssue, importRow, memberNameMap);

                    if (created) {
                        issueMapper.insert(targetIssue);
                        existingMap.put(descriptionKey, targetIssue);
                        addedCount++;
                        recordImportOperation(targetIssue.getId(), null, targetIssue.getStatus(), "通过 OPL 模板导入新增");
                        issueCommentService.createSystemComment(targetIssue.getId(), "通过 OPL 模板导入新增问题", targetIssue.getStatus());
                    } else {
                        issueMapper.updateById(targetIssue);
                        updatedCount++;
                        recordImportOperation(targetIssue.getId(), matchedIssue.getStatus(), targetIssue.getStatus(), "通过 OPL 模板导入覆盖更新");
                        issueCommentService.createSystemComment(targetIssue.getId(), "通过 OPL 模板导入覆盖更新主字段", targetIssue.getStatus());
                    }
                } catch (Exception exception) {
                    failedMessages.add("第 " + (rowIndex + 1) + " 行导入失败：" + exception.getMessage());
                }
            }

            return ProjectIssueImportResultVO.builder()
                    .totalCount(totalCount)
                    .addedCount(addedCount)
                    .updatedCount(updatedCount)
                    .skippedCount(skippedCount)
                    .failedCount(failedMessages.size())
                    .failedMessages(failedMessages)
                    .build();
        } catch (IOException exception) {
            throw new BusinessException("导入文件读取失败");
        }
    }

    private void validateImportFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请上传 Excel 文件");
        }
        String fileName = file.getOriginalFilename();
        String lowerCaseName = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        if (!(lowerCaseName.endsWith(".xlsx") || lowerCaseName.endsWith(".xls"))) {
            throw new BusinessException("仅支持导入 .xlsx / .xls 文件");
        }
    }

    private int locateHeaderRow(Sheet sheet) {
        int endRow = Math.min(sheet.getLastRowNum(), 10);
        for (int rowIndex = 0; rowIndex <= endRow; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            String marker = readCellString(row.getCell(2), new DataFormatter(Locale.getDefault()), null);
            if ("Description of the task".equalsIgnoreCase(marker.trim())) {
                return rowIndex;
            }
        }
        throw new BusinessException("导入格式错误：未识别到 OPL 模板表头");
    }

    private ImportRow parseImportRow(Row row, DataFormatter formatter, FormulaEvaluator evaluator) {
        return new ImportRow(
                parseDateTimeCell(row.getCell(1), formatter, evaluator),
                readCellString(row.getCell(2), formatter, evaluator),
                readCellString(row.getCell(3), formatter, evaluator),
                readCellString(row.getCell(4), formatter, evaluator),
                parseDateTimeCell(row.getCell(5), formatter, evaluator),
                readCellString(row.getCell(6), formatter, evaluator),
                readCellString(row.getCell(7), formatter, evaluator),
                readCellString(row.getCell(8), formatter, evaluator)
        );
    }

    private Issue buildImportedIssue(Project project, ImportRow importRow, Map<String, SysUser> memberNameMap) {
        Issue issue = new Issue();
        issue.setIssueNo(IssueNoGenerator.generate());
        issue.setProjectId(project.getId());
        issue.setProjectName(project.getProjectName());
        issue.setTitle(buildIssueTitle(importRow.description()));
        issue.setDescription(importRow.description().trim());
        issue.setActionPlan(trimToNull(importRow.keyResult()));
        issue.setPilotName(trimToNull(importRow.pilot()));
        issue.setLegacyStatus(trimToNull(importRow.status()));
        issue.setLegacyComment(trimToNull(importRow.comments()));
        issue.setOccurredAt(importRow.occurredAt() != null ? importRow.occurredAt() : LocalDateTime.now());
        issue.setDueAt(importRow.deadline());
        issue.setPriority(mapPriority(importRow.priority()).name());
        issue.setStatus(mapStatus(importRow.status()).getCode());
        issue.setCategoryCode("OTHER");
        issue.setSourceCode("EXCEL_IMPORT");
        issue.setImpactLevel("MEDIUM");
        issue.setAffectShipment(0);
        issue.setAffectCommissioning(0);
        issue.setNeedShutdown(0);
        issue.setReporterId(SecurityUtils.getCurrentUserId());
        issue.setReporterName(SecurityUtils.getCurrentRealName());
        issue.setLastFollowUpAt(LocalDateTime.now());
        issue.setVersion(0);
        applyOwnerFromPilot(issue, memberNameMap, importRow.pilot(), true);
        applyCloseFields(issue, issue.getStatus(), importRow);
        return issue;
    }

    private Issue applyImportedIssue(Issue issue, ImportRow importRow, Map<String, SysUser> memberNameMap) {
        String mappedStatus = mapStatus(importRow.status()).getCode();
        issue.setTitle(buildIssueTitle(importRow.description()));
        issue.setDescription(importRow.description().trim());
        issue.setActionPlan(trimToNull(importRow.keyResult()));
        issue.setPilotName(trimToNull(importRow.pilot()));
        issue.setLegacyStatus(trimToNull(importRow.status()));
        issue.setLegacyComment(trimToNull(importRow.comments()));
        if (importRow.occurredAt() != null) {
            issue.setOccurredAt(importRow.occurredAt());
        }
        issue.setDueAt(importRow.deadline());
        issue.setPriority(mapPriority(importRow.priority()).name());
        issue.setStatus(mappedStatus);
        issue.setProjectName(issue.getProjectName());
        issue.setLastFollowUpAt(LocalDateTime.now());
        applyOwnerFromPilot(issue, memberNameMap, importRow.pilot(), false);
        applyCloseFields(issue, mappedStatus, importRow);
        return issue;
    }

    private void applyOwnerFromPilot(Issue issue, Map<String, SysUser> memberNameMap, String pilotRaw, boolean clearIfUnmatchedForNew) {
        issue.setPilotName(trimToNull(pilotRaw));
        SysUser matchedOwner = resolvePilotOwner(memberNameMap, pilotRaw);
        if (matchedOwner != null) {
            issue.setOwnerId(matchedOwner.getId());
            issue.setOwnerName(matchedOwner.getRealName());
            issue.setOwnerDepartmentCode(matchedOwner.getDepartmentCode());
            issue.setOwnerDepartmentName(matchedOwner.getDepartmentName());
            issue.setCurrentHandlerId(matchedOwner.getId());
            issue.setCurrentHandlerName(matchedOwner.getRealName());
            return;
        }

        if (clearIfUnmatchedForNew) {
            issue.setOwnerId(null);
            issue.setOwnerName(null);
            issue.setOwnerDepartmentCode(null);
            issue.setOwnerDepartmentName(null);
            issue.setCurrentHandlerId(null);
            issue.setCurrentHandlerName(null);
        }
    }

    private void applyCloseFields(Issue issue, String mappedStatus, ImportRow importRow) {
        if (IssueStatusEnum.CLOSED.getCode().equals(mappedStatus)) {
            if (issue.getClosedAt() == null) {
                issue.setClosedAt(LocalDateTime.now());
            }
            issue.setClosedById(SecurityUtils.getCurrentUserId());
            issue.setClosedByName(SecurityUtils.getCurrentRealName());
            issue.setCloseReason(firstNonBlank(trimToNull(importRow.comments()), trimToNull(importRow.keyResult()), issue.getCloseReason()));
        } else {
            issue.setClosedAt(null);
            issue.setClosedById(null);
            issue.setClosedByName(null);
            issue.setCloseReason(null);
        }
    }

    private Map<String, Issue> loadExistingIssueMap(Long projectId) {
        List<Issue> issues = issueMapper.selectList(new LambdaQueryWrapper<Issue>()
                .eq(Issue::getProjectId, projectId)
                .orderByDesc(Issue::getUpdatedAt)
                .orderByDesc(Issue::getCreatedAt));
        Map<String, Issue> result = new LinkedHashMap<>();
        for (Issue issue : issues) {
            String descriptionKey = normalizeDescriptionKey(issue.getDescription());
            if (StringUtils.hasText(descriptionKey) && !result.containsKey(descriptionKey)) {
                result.put(descriptionKey, issue);
            }
        }
        return result;
    }

    private Map<String, SysUser> loadProjectMemberNameMap(Long projectId) {
        List<ProjectMember> members = projectMemberMapper.selectList(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId));
        if (members.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> userIds = members.stream().map(ProjectMember::getUserId).toList();
        Map<String, SysUser> map = new HashMap<>();
        sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>().in(SysUser::getId, userIds))
                .forEach(user -> {
                    putMemberAlias(map, user.getRealName(), user);
                    putMemberAlias(map, user.getUsername(), user);
                });
        return map;
    }

    private void putMemberAlias(Map<String, SysUser> map, String alias, SysUser user) {
        String key = normalizePersonKey(alias);
        if (StringUtils.hasText(key)) {
            map.putIfAbsent(key, user);
        }
    }

    private SysUser resolvePilotOwner(Map<String, SysUser> memberNameMap, String pilotRaw) {
        if (!StringUtils.hasText(pilotRaw)) {
            return null;
        }
        String normalized = pilotRaw.replace("\r", "\n").trim();
        String[] tokens = normalized.split("[\\n,/;，；]+");
        if (tokens.length != 1) {
            return null;
        }
        return memberNameMap.get(normalizePersonKey(tokens[0]));
    }

    private String buildIssueTitle(String description) {
        String normalized = firstNonBlank(description).replace("\r", "\n");
        String firstLine = normalized.contains("\n") ? normalized.substring(0, normalized.indexOf('\n')) : normalized;
        firstLine = firstLine.trim();
        if (firstLine.length() > 80) {
            return firstLine.substring(0, 80);
        }
        return firstLine;
    }

    private String normalizeDescriptionKey(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.replace("\r", "\n")
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizePersonKey(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.replaceAll("\\s+", " ").trim().toLowerCase(Locale.ROOT);
    }

    private String readCellString(Cell cell, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (cell == null) {
            return "";
        }
        String value = evaluator == null ? formatter.formatCellValue(cell) : formatter.formatCellValue(cell, evaluator);
        return value == null ? "" : value.trim();
    }

    private LocalDateTime parseDateTimeCell(Cell cell, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (cell == null) {
            return null;
        }
        try {
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue();
            }
        } catch (Exception ignored) {
            // Fallback to text parsing below.
        }
        String value = readCellString(cell, formatter, evaluator);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        value = value.replace('T', ' ').trim();
        for (DateTimeFormatter formatterItem : DATE_PATTERNS) {
            try {
                return LocalDateTime.parse(value, formatterItem);
            } catch (DateTimeParseException ignored) {
                try {
                    return LocalDate.parse(value, formatterItem).atStartOfDay();
                } catch (DateTimeParseException ignoredAgain) {
                    // continue
                }
            }
        }
        return null;
    }

    private IssuePriorityEnum mapPriority(String raw) {
        String value = firstNonBlank(raw).toUpperCase(Locale.ROOT);
        return switch (value) {
            case "A" -> IssuePriorityEnum.HIGH;
            case "B" -> IssuePriorityEnum.MEDIUM;
            case "C" -> IssuePriorityEnum.LOW;
            default -> IssuePriorityEnum.MEDIUM;
        };
    }

    private IssueStatusEnum mapStatus(String raw) {
        String value = firstNonBlank(raw).toLowerCase(Locale.ROOT);
        if (value.contains("done") || value.contains("closed")) {
            return IssueStatusEnum.CLOSED;
        }
        if (value.contains("cancel")) {
            return IssueStatusEnum.CANCELED;
        }
        if (value.contains("new")) {
            return IssueStatusEnum.NEW;
        }
        return IssueStatusEnum.IN_PROGRESS;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private void recordImportOperation(Long issueId, String fromValue, String toValue, String remark) {
        IssueOperationLog log = new IssueOperationLog();
        log.setIssueId(issueId);
        log.setOperationType(IssueOperationTypeEnum.IMPORT.getCode());
        log.setFromValue(fromValue);
        log.setToValue(toValue);
        log.setRemark(remark);
        log.setOperatorId(SecurityUtils.getCurrentUserId());
        log.setOperatorName(SecurityUtils.getCurrentRealName());
        issueOperationLogMapper.insert(log);
    }

    private record ImportRow(LocalDateTime occurredAt,
                             String description,
                             String priority,
                             String keyResult,
                             LocalDateTime deadline,
                             String pilot,
                             String status,
                             String comments) {
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
                .eq(StringUtils.hasText(query.getIssueFunctionCode()), Issue::getIssueFunctionCode, query.getIssueFunctionCode())
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
        exportQuery.setIssueFunctionCode(query.getIssueFunctionCode());
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
