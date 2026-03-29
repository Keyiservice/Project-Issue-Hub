package com.company.opl.dto.issue;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IssueAssignDTO {
    @NotNull(message = "责任人不能为空")
    private Long ownerId;
    private String remark;
}

