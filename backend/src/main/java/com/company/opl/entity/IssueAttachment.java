package com.company.opl.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.company.opl.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("issue_attachment")
@EqualsAndHashCode(callSuper = true)
public class IssueAttachment extends BaseEntity {

    @TableId
    private Long id;
    private Long issueId;
    private Long commentId;
    private String storageProvider;
    private String bucketName;
    private String objectKey;
    private String fileName;
    private String fileExt;
    private String fileType;
    private String contentType;
    private Long fileSize;
    private String fileUrl;
    private Long uploadedBy;
    private String uploadedByName;
    private LocalDateTime uploadedAt;
}

