package com.company.opl.vo.issue;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class IssueCommentVO {
    private Long id;
    private String commentType;
    private String content;
    private String statusSnapshot;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime createdAt;
    private List<IssueAttachmentVO> attachments;
}

