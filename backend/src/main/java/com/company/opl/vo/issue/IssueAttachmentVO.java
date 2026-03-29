package com.company.opl.vo.issue;

import lombok.Data;

@Data
public class IssueAttachmentVO {
    private Long id;
    private Long commentId;
    private String fileName;
    private String objectKey;
    private String fileUrl;
    private String fileType;
    private String contentType;
    private Long fileSize;
}

