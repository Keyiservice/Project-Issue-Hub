package com.company.opl.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileUploadVO {
    private String fileName;
    private String objectKey;
    private String fileUrl;
    private Long fileSize;
    private String contentType;
}

