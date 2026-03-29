package com.company.opl.dto.issue;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IssuePriorityUpdateDTO {
    @NotBlank(message = "优先级不能为空")
    private String priority;
    private String remark;
}

