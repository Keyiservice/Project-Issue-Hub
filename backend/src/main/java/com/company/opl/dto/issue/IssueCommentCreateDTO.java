package com.company.opl.dto.issue;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class IssueCommentCreateDTO {
    @NotBlank(message = "评论内容不能为空")
    private String content;
    private List<IssueAttachmentDTO> attachments;
}

