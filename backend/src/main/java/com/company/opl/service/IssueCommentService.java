package com.company.opl.service;

import com.company.opl.dto.issue.IssueCommentCreateDTO;
import com.company.opl.entity.IssueComment;
import com.company.opl.vo.issue.IssueCommentVO;

import java.util.List;

public interface IssueCommentService {

    Long createComment(Long issueId, IssueCommentCreateDTO dto);

    IssueComment createSystemComment(Long issueId, String content, String statusSnapshot);

    List<IssueCommentVO> listByIssueId(Long issueId);
}
