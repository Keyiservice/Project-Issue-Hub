package com.company.opl.service;

import com.company.opl.dto.issue.IssueStatusChangeDTO;

public interface IssueFlowService {

    void changeStatus(Long issueId, IssueStatusChangeDTO dto);
}

