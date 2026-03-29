package com.company.opl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.opl.dto.issue.IssueAttachmentDTO;
import com.company.opl.dto.issue.IssueCommentCreateDTO;
import com.company.opl.entity.Issue;
import com.company.opl.entity.IssueAttachment;
import com.company.opl.entity.IssueComment;
import com.company.opl.entity.IssueOperationLog;
import com.company.opl.enums.IssueCommentTypeEnum;
import com.company.opl.enums.IssueOperationTypeEnum;
import com.company.opl.exception.BusinessException;
import com.company.opl.mapper.IssueAttachmentMapper;
import com.company.opl.mapper.IssueCommentMapper;
import com.company.opl.mapper.IssueMapper;
import com.company.opl.mapper.IssueOperationLogMapper;
import com.company.opl.service.IssueCommentService;
import com.company.opl.util.FileTypeUtils;
import com.company.opl.util.SecurityUtils;
import com.company.opl.vo.issue.IssueAttachmentVO;
import com.company.opl.vo.issue.IssueCommentVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class IssueCommentServiceImpl implements IssueCommentService {

    private final IssueMapper issueMapper;
    private final IssueCommentMapper issueCommentMapper;
    private final IssueAttachmentMapper issueAttachmentMapper;
    private final IssueOperationLogMapper issueOperationLogMapper;

    public IssueCommentServiceImpl(IssueMapper issueMapper,
                                   IssueCommentMapper issueCommentMapper,
                                   IssueAttachmentMapper issueAttachmentMapper,
                                   IssueOperationLogMapper issueOperationLogMapper) {
        this.issueMapper = issueMapper;
        this.issueCommentMapper = issueCommentMapper;
        this.issueAttachmentMapper = issueAttachmentMapper;
        this.issueOperationLogMapper = issueOperationLogMapper;
    }

    @Override
    public Long createComment(Long issueId, IssueCommentCreateDTO dto) {
        Issue issue = issueMapper.selectById(issueId);
        if (issue == null) {
            throw new BusinessException("问题不存在");
        }
        IssueComment comment = buildComment(issueId, dto.getContent(), issue.getStatus(), IssueCommentTypeEnum.COMMENT.name());
        issueCommentMapper.insert(comment);
        saveAttachments(issueId, comment.getId(), dto.getAttachments());
        issue.setLastFollowUpAt(LocalDateTime.now());
        issueMapper.updateById(issue);
        recordCommentLog(issueId, dto.getContent());
        return comment.getId();
    }

    @Override
    public IssueComment createSystemComment(Long issueId, String content, String statusSnapshot) {
        IssueComment comment = buildComment(issueId, content, statusSnapshot, IssueCommentTypeEnum.SYSTEM.name());
        issueCommentMapper.insert(comment);
        return comment;
    }

    @Override
    public List<IssueCommentVO> listByIssueId(Long issueId) {
        List<IssueComment> comments = issueCommentMapper.selectList(new LambdaQueryWrapper<IssueComment>()
                .eq(IssueComment::getIssueId, issueId)
                .orderByDesc(IssueComment::getCreatedAt));
        if (comments.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> commentIds = comments.stream().map(IssueComment::getId).toList();
        Map<Long, List<IssueAttachmentVO>> attachmentMap = issueAttachmentMapper.selectList(new LambdaQueryWrapper<IssueAttachment>()
                        .eq(IssueAttachment::getIssueId, issueId)
                        .in(IssueAttachment::getCommentId, commentIds))
                .stream()
                .map(this::toAttachmentVO)
                .collect(Collectors.groupingBy(IssueAttachmentVO::getCommentId));

        return comments.stream().map(comment -> {
            IssueCommentVO vo = new IssueCommentVO();
            vo.setId(comment.getId());
            vo.setCommentType(comment.getCommentType());
            vo.setContent(comment.getContent());
            vo.setStatusSnapshot(comment.getStatusSnapshot());
            vo.setOperatorId(comment.getOperatorId());
            vo.setOperatorName(comment.getOperatorName());
            vo.setCreatedAt(comment.getCreatedAt());
            vo.setAttachments(attachmentMap.getOrDefault(comment.getId(), Collections.emptyList()));
            return vo;
        }).toList();
    }

    private IssueComment buildComment(Long issueId, String content, String statusSnapshot, String commentType) {
        IssueComment comment = new IssueComment();
        comment.setIssueId(issueId);
        comment.setCommentType(commentType);
        comment.setContent(content);
        comment.setStatusSnapshot(statusSnapshot);
        comment.setOperatorId(SecurityUtils.getCurrentUserId());
        comment.setOperatorName(SecurityUtils.getCurrentRealName());
        return comment;
    }

    private void saveAttachments(Long issueId, Long commentId, List<IssueAttachmentDTO> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return;
        }
        attachments.forEach(item -> {
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
            issueAttachmentMapper.insert(attachment);
        });
    }

    private void recordCommentLog(Long issueId, String content) {
        IssueOperationLog operationLog = new IssueOperationLog();
        operationLog.setIssueId(issueId);
        operationLog.setOperationType(IssueOperationTypeEnum.COMMENT.getCode());
        operationLog.setRemark(content);
        operationLog.setOperatorId(SecurityUtils.getCurrentUserId());
        operationLog.setOperatorName(SecurityUtils.getCurrentRealName());
        issueOperationLogMapper.insert(operationLog);
    }

    private IssueAttachmentVO toAttachmentVO(IssueAttachment attachment) {
        IssueAttachmentVO vo = new IssueAttachmentVO();
        vo.setId(attachment.getId());
        vo.setCommentId(attachment.getCommentId());
        vo.setFileName(attachment.getFileName());
        vo.setObjectKey(attachment.getObjectKey());
        vo.setFileUrl(attachment.getFileUrl());
        vo.setFileType(attachment.getFileType());
        vo.setContentType(attachment.getContentType());
        vo.setFileSize(attachment.getFileSize());
        return vo;
    }
}
