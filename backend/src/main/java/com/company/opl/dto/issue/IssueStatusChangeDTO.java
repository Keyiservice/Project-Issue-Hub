package com.company.opl.dto.issue;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IssueStatusChangeDTO {
    @NotBlank(message = "目标状态不能为空")
    private String targetStatus;
    private String remark;
    private String holdReason;
    private String closeReason;
    private String closeEvidence;
    private String solutionDesc;
}
