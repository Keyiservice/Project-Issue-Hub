package com.company.opl.dto.issue;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IssueAttachmentDTO {
    @NotBlank(message = "文件名不能为空")
    private String fileName;
    @NotBlank(message = "对象Key不能为空")
    private String objectKey;
    @NotBlank(message = "访问地址不能为空")
    private String fileUrl;
    @NotNull(message = "文件大小不能为空")
    private Long fileSize;
    private String contentType;
}

