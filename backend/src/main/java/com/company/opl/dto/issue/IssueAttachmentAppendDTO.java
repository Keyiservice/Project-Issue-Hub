package com.company.opl.dto.issue;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class IssueAttachmentAppendDTO {

    @Valid
    @NotEmpty(message = "至少上传一个附件")
    private List<IssueAttachmentDTO> attachments;

    private String remark;
}
