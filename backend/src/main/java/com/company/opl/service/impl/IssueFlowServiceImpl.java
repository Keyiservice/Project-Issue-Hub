package com.company.opl.service.impl;

import com.company.opl.dto.issue.IssueStatusChangeDTO;
import com.company.opl.entity.Issue;
import com.company.opl.entity.IssueOperationLog;
import com.company.opl.enums.IssueOperationTypeEnum;
import com.company.opl.enums.IssueStatusEnum;
import com.company.opl.exception.BusinessException;
import com.company.opl.mapper.IssueMapper;
import com.company.opl.mapper.IssueOperationLogMapper;
import com.company.opl.service.IssueCommentService;
import com.company.opl.service.IssueFlowService;
import com.company.opl.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Service
public class IssueFlowServiceImpl implements IssueFlowService {

    private static final Map<IssueStatusEnum, Set<IssueStatusEnum>> STATE_FLOW = Map.of(
            IssueStatusEnum.NEW, Set.of(IssueStatusEnum.IN_PROGRESS, IssueStatusEnum.ON_HOLD, IssueStatusEnum.CANCELED),
            IssueStatusEnum.ACCEPTED, Set.of(IssueStatusEnum.IN_PROGRESS, IssueStatusEnum.PENDING_VERIFY, IssueStatusEnum.ON_HOLD, IssueStatusEnum.CANCELED),
            IssueStatusEnum.IN_PROGRESS, Set.of(IssueStatusEnum.PENDING_VERIFY, IssueStatusEnum.ON_HOLD, IssueStatusEnum.CANCELED),
            IssueStatusEnum.PENDING_VERIFY, Set.of(IssueStatusEnum.CLOSED, IssueStatusEnum.IN_PROGRESS),
            IssueStatusEnum.ON_HOLD, Set.of(IssueStatusEnum.IN_PROGRESS, IssueStatusEnum.CANCELED),
            IssueStatusEnum.CLOSED, Set.of(),
            IssueStatusEnum.CANCELED, Set.of()
    );

    private final IssueMapper issueMapper;
    private final IssueOperationLogMapper operationLogMapper;
    private final IssueCommentService issueCommentService;

    public IssueFlowServiceImpl(IssueMapper issueMapper,
                                IssueOperationLogMapper operationLogMapper,
                                IssueCommentService issueCommentService) {
        this.issueMapper = issueMapper;
        this.operationLogMapper = operationLogMapper;
        this.issueCommentService = issueCommentService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long issueId, IssueStatusChangeDTO dto) {
        Issue issue = issueMapper.selectById(issueId);
        if (issue == null) {
            throw new BusinessException("问题不存在");
        }

        IssueStatusEnum currentStatus = IssueStatusEnum.fromCode(issue.getStatus());
        IssueStatusEnum targetStatus = IssueStatusEnum.fromCode(dto.getTargetStatus());
        if (!STATE_FLOW.getOrDefault(currentStatus, Set.of()).contains(targetStatus)) {
            throw new BusinessException("当前状态不允许流转到目标状态");
        }

        validateTransition(issue, currentStatus, targetStatus, dto);

        issue.setStatus(targetStatus.getCode());
        issue.setLastFollowUpAt(LocalDateTime.now());

        if (targetStatus == IssueStatusEnum.ON_HOLD) {
            issue.setHoldReason(dto.getHoldReason());
        } else {
            issue.setHoldReason(null);
        }

        if (targetStatus == IssueStatusEnum.CLOSED) {
            issue.setCloseReason(dto.getCloseReason());
            issue.setCloseEvidence(dto.getCloseEvidence());
            issue.setClosedAt(LocalDateTime.now());
            issue.setClosedById(SecurityUtils.getCurrentUserId());
            issue.setClosedByName(SecurityUtils.getCurrentRealName());
        }

        if (targetStatus == IssueStatusEnum.IN_PROGRESS && currentStatus == IssueStatusEnum.PENDING_VERIFY) {
            issue.setCloseReason(null);
            issue.setCloseEvidence(null);
            issue.setClosedAt(null);
            issue.setClosedById(null);
            issue.setClosedByName(null);
        }

        issueMapper.updateById(issue);
        recordStatusLog(issueId, currentStatus.getCode(), targetStatus.getCode(), dto.getRemark());
        issueCommentService.createSystemComment(issueId, buildSystemComment(currentStatus, targetStatus, dto), targetStatus.getCode());
    }

    private void validateTransition(Issue issue,
                                    IssueStatusEnum currentStatus,
                                    IssueStatusEnum targetStatus,
                                    IssueStatusChangeDTO dto) {
        if (targetStatus == IssueStatusEnum.IN_PROGRESS && issue.getOwnerId() == null) {
            throw new BusinessException("进入处理中前必须指定责任人");
        }
        if (targetStatus == IssueStatusEnum.PENDING_VERIFY && !StringUtils.hasText(dto.getSolutionDesc())) {
            throw new BusinessException("提交待验证前必须填写处理说明");
        }
        if (targetStatus == IssueStatusEnum.ON_HOLD && !StringUtils.hasText(dto.getHoldReason())) {
            throw new BusinessException("挂起前必须填写挂起原因");
        }
        if (currentStatus == IssueStatusEnum.PENDING_VERIFY
                && targetStatus == IssueStatusEnum.IN_PROGRESS
                && !StringUtils.hasText(dto.getRemark())) {
            throw new BusinessException("验证打回处理中时必须填写原因");
        }
    }

    private String buildSystemComment(IssueStatusEnum currentStatus, IssueStatusEnum targetStatus, IssueStatusChangeDTO dto) {
        StringBuilder builder = new StringBuilder("状态由 ")
                .append(currentStatus.getCode())
                .append(" 变更为 ")
                .append(targetStatus.getCode());
        if (StringUtils.hasText(dto.getSolutionDesc())) {
            builder.append("；处理说明：").append(dto.getSolutionDesc());
        }
        if (StringUtils.hasText(dto.getHoldReason())) {
            builder.append("；挂起原因：").append(dto.getHoldReason());
        }
        if (StringUtils.hasText(dto.getCloseReason())) {
            builder.append("；关闭说明：").append(dto.getCloseReason());
        }
        if (StringUtils.hasText(dto.getCloseEvidence())) {
            builder.append("；关闭证据：").append(dto.getCloseEvidence());
        }
        if (StringUtils.hasText(dto.getRemark())) {
            builder.append("；备注：").append(dto.getRemark());
        }
        return builder.toString();
    }

    private void recordStatusLog(Long issueId, String fromStatus, String toStatus, String remark) {
        IssueOperationLog log = new IssueOperationLog();
        log.setIssueId(issueId);
        log.setOperationType(IssueOperationTypeEnum.STATUS_CHANGE.getCode());
        log.setFromValue(fromStatus);
        log.setToValue(toStatus);
        log.setRemark(remark);
        log.setOperatorId(SecurityUtils.getCurrentUserId());
        log.setOperatorName(SecurityUtils.getCurrentRealName());
        operationLogMapper.insert(log);
    }
}
