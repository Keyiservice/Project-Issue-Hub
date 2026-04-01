package com.company.opl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.opl.common.PageResult;
import com.company.opl.dto.issue.IssueAssignDTO;
import com.company.opl.dto.issue.IssueAttachmentAppendDTO;
import com.company.opl.dto.issue.IssueAttachmentDTO;
import com.company.opl.dto.issue.IssueCreateDTO;
import com.company.opl.dto.issue.IssuePriorityUpdateDTO;
import com.company.opl.entity.Issue;
import com.company.opl.entity.IssueAttachment;
import com.company.opl.entity.IssueComment;
import com.company.opl.entity.IssueOperationLog;
import com.company.opl.entity.SysUser;
import com.company.opl.enums.IssueOperationTypeEnum;
import com.company.opl.enums.IssuePriorityEnum;
import com.company.opl.enums.IssueStatusEnum;
import com.company.opl.enums.UserStatusEnum;
import com.company.opl.exception.BusinessException;
import com.company.opl.mapper.IssueAttachmentMapper;
import com.company.opl.mapper.IssueCommentMapper;
import com.company.opl.mapper.IssueMapper;
import com.company.opl.mapper.IssueOperationLogMapper;
import com.company.opl.mapper.SysUserMapper;
import com.company.opl.query.issue.IssueQuery;
import com.company.opl.service.FileStorageService;
import com.company.opl.service.IssueCommentService;
import com.company.opl.service.IssueService;
import com.company.opl.service.ProjectMemberService;
import com.company.opl.util.FileTypeUtils;
import com.company.opl.util.IssueNoGenerator;
import com.company.opl.util.SecurityUtils;
import com.company.opl.vo.issue.IssueAttachmentVO;
import com.company.opl.vo.issue.IssueDetailVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class IssueServiceImpl extends ServiceImpl<IssueMapper, Issue> implements IssueService {

    private final IssueOperationLogMapper operationLogMapper;
    private final IssueAttachmentMapper issueAttachmentMapper;
    private final IssueCommentMapper issueCommentMapper;
    private final IssueCommentService issueCommentService;
    private final SysUserMapper sysUserMapper;
    private final FileStorageService fileStorageService;
    private final ProjectMemberService projectMemberService;

    public IssueServiceImpl(IssueOperationLogMapper operationLogMapper,
                            IssueAttachmentMapper issueAttachmentMapper,
                            IssueCommentMapper issueCommentMapper,
                            IssueCommentService issueCommentService,
                            SysUserMapper sysUserMapper,
                            FileStorageService fileStorageService,
                            ProjectMemberService projectMemberService) {
        this.operationLogMapper = operationLogMapper;
        this.issueAttachmentMapper = issueAttachmentMapper;
        this.issueCommentMapper = issueCommentMapper;
        this.issueCommentService = issueCommentService;
        this.sysUserMapper = sysUserMapper;
        this.fileStorageService = fileStorageService;
        this.projectMemberService = projectMemberService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createIssue(IssueCreateDTO dto) {
        Issue issue = new Issue();
        BeanUtils.copyProperties(dto, issue);
        issue.setIssueNo(IssueNoGenerator.generate());
        issue.setStatus(IssueStatusEnum.NEW.getCode());
        issue.setAffectShipment(Boolean.TRUE.equals(dto.getAffectShipment()) ? 1 : 0);
        issue.setAffectCommissioning(Boolean.TRUE.equals(dto.getAffectCommissioning()) ? 1 : 0);
        issue.setNeedShutdown(Boolean.TRUE.equals(dto.getNeedShutdown()) ? 1 : 0);
        issue.setReporterId(SecurityUtils.getCurrentUserId());
        issue.setReporterName(SecurityUtils.getCurrentRealName());
        fillOwner(issue, dto.getProjectId(), dto.getOwnerId());
        issue.setLastFollowUpAt(LocalDateTime.now());
        issue.setVersion(0);
        save(issue);

        saveIssueAttachments(issue.getId(), dto.getAttachments());
        issueCommentService.createSystemComment(issue.getId(), "问题已创建", IssueStatusEnum.NEW.getCode());
        recordOperation(issue.getId(), IssueOperationTypeEnum.CREATE.getCode(), null, IssueStatusEnum.NEW.getCode(), "问题创建");
        return issue.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignIssue(Long issueId, IssueAssignDTO dto) {
        Issue issue = loadIssue(issueId);
        SysUser owner = loadEnabledUser(dto.getOwnerId());
        projectMemberService.ensureProjectMember(issue.getProjectId(), owner.getId());

        String fromValue = issue.getOwnerName();
        issue.setOwnerId(owner.getId());
        issue.setOwnerName(owner.getRealName());
        issue.setOwnerDepartmentCode(owner.getDepartmentCode());
        issue.setOwnerDepartmentName(owner.getDepartmentName());
        issue.setCurrentHandlerId(owner.getId());
        issue.setCurrentHandlerName(owner.getRealName());
        issue.setLastFollowUpAt(LocalDateTime.now());
        updateById(issue);

        String remark = StringUtils.hasText(dto.getRemark()) ? dto.getRemark() : "责任人调整为 " + owner.getRealName();
        recordOperation(issueId, IssueOperationTypeEnum.ASSIGN.getCode(), fromValue, owner.getRealName(), remark);
        issueCommentService.createSystemComment(issueId, "责任人变更为 " + owner.getRealName() + appendRemark(dto.getRemark()), issue.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePriority(Long issueId, IssuePriorityUpdateDTO dto) {
        Issue issue = loadIssue(issueId);
        validatePriority(dto.getPriority());

        String fromValue = issue.getPriority();
        if (dto.getPriority().equals(fromValue)) {
            return;
        }

        issue.setPriority(dto.getPriority());
        issue.setLastFollowUpAt(LocalDateTime.now());
        updateById(issue);

        String remark = StringUtils.hasText(dto.getRemark()) ? dto.getRemark() : "优先级调整为 " + dto.getPriority();
        recordOperation(issueId, IssueOperationTypeEnum.PRIORITY_CHANGE.getCode(), fromValue, dto.getPriority(), remark);
        issueCommentService.createSystemComment(issueId, "优先级由 " + fromValue + " 调整为 " + dto.getPriority() + appendRemark(dto.getRemark()), issue.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void appendAttachments(Long issueId, IssueAttachmentAppendDTO dto) {
        Issue issue = loadIssue(issueId);
        saveIssueAttachments(issueId, dto.getAttachments());
        issue.setLastFollowUpAt(LocalDateTime.now());
        updateById(issue);

        String content = "补充了 " + dto.getAttachments().size() + " 个附件" + appendRemark(dto.getRemark());
        recordOperation(issueId, IssueOperationTypeEnum.ATTACHMENT_APPEND.getCode(), null, null, content);
        issueCommentService.createSystemComment(issueId, content, issue.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAttachment(Long issueId, Long attachmentId) {
        Issue issue = loadIssue(issueId);
        IssueAttachment attachment = issueAttachmentMapper.selectById(attachmentId);
        if (attachment == null || !issueId.equals(attachment.getIssueId())) {
            throw new BusinessException("附件不存在");
        }
        if (attachment.getCommentId() != null) {
            throw new BusinessException("评论附件请在时间线中处理");
        }

        validateAttachmentDeletePermission(issue, attachment);
        issueAttachmentMapper.deleteById(attachmentId);

        issue.setLastFollowUpAt(LocalDateTime.now());
        updateById(issue);

        String content = "删除了附件 " + attachment.getFileName();
        recordOperation(issueId, IssueOperationTypeEnum.ATTACHMENT_DELETE.getCode(), attachment.getFileName(), null, content);
        issueCommentService.createSystemComment(issueId, content, issue.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteIssue(Long issueId) {
        Issue issue = loadIssue(issueId);
        validateDeletePermission(issue);

        recordOperation(issueId, IssueOperationTypeEnum.DELETE.getCode(), issue.getStatus(), "DELETED", "误录删除");
        issueCommentService.createSystemComment(issueId, "问题已按误录删除", issue.getStatus());

        issueAttachmentMapper.delete(new LambdaQueryWrapper<IssueAttachment>()
                .eq(IssueAttachment::getIssueId, issueId));
        issueCommentMapper.delete(new LambdaQueryWrapper<IssueComment>()
                .eq(IssueComment::getIssueId, issueId));
        removeById(issueId);
    }

    @Override
    public PageResult<IssueDetailVO> pageIssues(IssueQuery query) {
        LambdaQueryWrapper<Issue> wrapper = new LambdaQueryWrapper<Issue>()
                .eq(query.getProjectId() != null, Issue::getProjectId, query.getProjectId())
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
                        .like(Issue::getDescription, query.getKeyword()))
                .in(query.getStatusList() != null && !query.getStatusList().isEmpty(), Issue::getStatus, query.getStatusList())
                .orderByDesc(Issue::getCreatedAt);

        if (Boolean.TRUE.equals(query.getOverdueOnly())) {
            wrapper.lt(Issue::getDueAt, LocalDateTime.now())
                    .ne(Issue::getStatus, IssueStatusEnum.CLOSED.getCode())
                    .ne(Issue::getStatus, IssueStatusEnum.CANCELED.getCode());
        }

        Page<Issue> page = page(new Page<>(query.getCurrent(), query.getSize()), wrapper);
        List<IssueDetailVO> records = page.getRecords().stream().map(this::toSummaryVO).toList();
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    @Override
    public IssueDetailVO getIssueDetail(Long issueId) {
        return toDetailVO(loadIssue(issueId));
    }

    private Issue loadIssue(Long issueId) {
        Issue issue = getById(issueId);
        if (issue == null) {
            throw new BusinessException("问题不存在");
        }
        return issue;
    }

    private IssueDetailVO toSummaryVO(Issue issue) {
        IssueDetailVO vo = new IssueDetailVO();
        BeanUtils.copyProperties(issue, vo);
        fillFlags(issue, vo);
        vo.setAttachments(Collections.emptyList());
        vo.setComments(Collections.emptyList());
        return vo;
    }

    private IssueDetailVO toDetailVO(Issue issue) {
        IssueDetailVO vo = new IssueDetailVO();
        BeanUtils.copyProperties(issue, vo);
        fillFlags(issue, vo);
        vo.setAttachments(issueAttachmentMapper.selectList(new LambdaQueryWrapper<IssueAttachment>()
                        .eq(IssueAttachment::getIssueId, issue.getId())
                        .isNull(IssueAttachment::getCommentId)
                        .orderByDesc(IssueAttachment::getCreatedAt))
                .stream()
                .map(this::toAttachmentVO)
                .toList());
        vo.setComments(issueCommentService.listByIssueId(issue.getId()));
        return vo;
    }

    private void fillFlags(Issue issue, IssueDetailVO vo) {
        vo.setAffectShipment(issue.getAffectShipment() != null && issue.getAffectShipment() == 1);
        vo.setAffectCommissioning(issue.getAffectCommissioning() != null && issue.getAffectCommissioning() == 1);
        vo.setNeedShutdown(issue.getNeedShutdown() != null && issue.getNeedShutdown() == 1);
        vo.setOverdue(issue.getDueAt() != null
                && issue.getDueAt().isBefore(LocalDateTime.now())
                && !IssueStatusEnum.CLOSED.getCode().equals(issue.getStatus())
                && !IssueStatusEnum.CANCELED.getCode().equals(issue.getStatus()));
    }

    private void fillOwner(Issue issue, Long projectId, Long ownerId) {
        if (ownerId == null) {
            issue.setOwnerId(null);
            issue.setOwnerName(null);
            issue.setOwnerDepartmentCode(null);
            issue.setOwnerDepartmentName(null);
            issue.setCurrentHandlerId(null);
            issue.setCurrentHandlerName(null);
            return;
        }

        SysUser owner = loadEnabledUser(ownerId);
        projectMemberService.ensureProjectMember(projectId, owner.getId());
        issue.setOwnerId(owner.getId());
        issue.setOwnerName(owner.getRealName());
        issue.setOwnerDepartmentCode(owner.getDepartmentCode());
        issue.setOwnerDepartmentName(owner.getDepartmentName());
        issue.setCurrentHandlerId(owner.getId());
        issue.setCurrentHandlerName(owner.getRealName());
    }

    private void saveIssueAttachments(Long issueId, List<IssueAttachmentDTO> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return;
        }

        attachments.forEach(item -> issueAttachmentMapper.insert(buildAttachment(issueId, null, item)));
    }

    private IssueAttachment buildAttachment(Long issueId, Long commentId, IssueAttachmentDTO item) {
        IssueAttachment attachment = new IssueAttachment();
        attachment.setIssueId(issueId);
        attachment.setCommentId(commentId);
        attachment.setStorageProvider("MINIO");
        attachment.setBucketName("opl-attachments");
        attachment.setObjectKey(item.getObjectKey());
        attachment.setFileName(item.getFileName());
        attachment.setFileExt(FileTypeUtils.resolveFileExt(item.getFileName()));
        attachment.setFileType(FileTypeUtils.resolveFileType(item.getContentType(), item.getFileName()));
        attachment.setContentType(item.getContentType());
        attachment.setFileSize(item.getFileSize());
        attachment.setFileUrl(item.getFileUrl());
        attachment.setUploadedBy(SecurityUtils.getCurrentUserId());
        attachment.setUploadedByName(SecurityUtils.getCurrentRealName());
        attachment.setUploadedAt(LocalDateTime.now());
        return attachment;
    }

    private IssueAttachmentVO toAttachmentVO(IssueAttachment attachment) {
        IssueAttachmentVO vo = new IssueAttachmentVO();
        vo.setId(attachment.getId());
        vo.setCommentId(attachment.getCommentId());
        vo.setFileName(attachment.getFileName());
        vo.setObjectKey(attachment.getObjectKey());
        vo.setFileUrl("/api/attachments/" + attachment.getId() + "/content");
        vo.setFileType(attachment.getFileType());
        vo.setContentType(attachment.getContentType());
        vo.setFileSize(attachment.getFileSize());
        return vo;
    }

    private void recordOperation(Long issueId, String operationType, String fromValue, String toValue, String remark) {
        IssueOperationLog log = new IssueOperationLog();
        log.setIssueId(issueId);
        log.setOperationType(operationType);
        log.setFromValue(fromValue);
        log.setToValue(toValue);
        log.setRemark(remark);
        log.setOperatorId(SecurityUtils.getCurrentUserId());
        log.setOperatorName(SecurityUtils.getCurrentRealName());
        operationLogMapper.insert(log);
    }

    private SysUser loadEnabledUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1 || !UserStatusEnum.ENABLED.name().equals(user.getStatus())) {
            throw new BusinessException("责任人不存在或未启用");
        }
        return user;
    }

    private void validateDeletePermission(Issue issue) {
        List<String> roles = SecurityUtils.getCurrentRoles();
        if (roles.contains("ADMIN") || roles.contains("PROJECT_MANAGER")) {
            return;
        }

        boolean reporterCanDelete = SecurityUtils.getCurrentUserId().equals(issue.getReporterId())
                && IssueStatusEnum.NEW.getCode().equals(issue.getStatus())
                && issue.getOwnerId() == null;
        if (reporterCanDelete) {
            return;
        }

        throw new BusinessException("只有管理员、项目经理，或提报人本人删除未流转问题");
    }

    private void validateAttachmentDeletePermission(Issue issue, IssueAttachment attachment) {
        List<String> roles = SecurityUtils.getCurrentRoles();
        if (roles.contains("ADMIN") || roles.contains("PROJECT_MANAGER")) {
            return;
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId.equals(issue.getReporterId()) || currentUserId.equals(attachment.getUploadedBy())) {
            return;
        }

        throw new BusinessException("只有管理员、项目经理、提报人或上传人可以删除附件");
    }

    private String appendRemark(String remark) {
        return StringUtils.hasText(remark) ? "；备注：" + remark : "";
    }

    private void validatePriority(String priority) {
        for (IssuePriorityEnum item : IssuePriorityEnum.values()) {
            if (item.name().equals(priority)) {
                return;
            }
        }
        throw new BusinessException("无效的优先级");
    }
}
