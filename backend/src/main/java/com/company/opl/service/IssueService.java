package com.company.opl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.company.opl.common.PageResult;
import com.company.opl.dto.issue.IssueAssignDTO;
import com.company.opl.dto.issue.IssueAttachmentAppendDTO;
import com.company.opl.dto.issue.IssueCreateDTO;
import com.company.opl.dto.issue.IssuePriorityUpdateDTO;
import com.company.opl.entity.Issue;
import com.company.opl.query.issue.IssueQuery;
import com.company.opl.vo.issue.IssueDetailVO;

public interface IssueService extends IService<Issue> {

    Long createIssue(IssueCreateDTO dto);

    void assignIssue(Long issueId, IssueAssignDTO dto);

    void updatePriority(Long issueId, IssuePriorityUpdateDTO dto);

    void appendAttachments(Long issueId, IssueAttachmentAppendDTO dto);

    void deleteAttachment(Long issueId, Long attachmentId);

    void deleteIssue(Long issueId);

    PageResult<IssueDetailVO> pageIssues(IssueQuery query);

    IssueDetailVO getIssueDetail(Long issueId);
}
