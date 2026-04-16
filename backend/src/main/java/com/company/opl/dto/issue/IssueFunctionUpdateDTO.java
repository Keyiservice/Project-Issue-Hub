package com.company.opl.dto.issue;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IssueFunctionUpdateDTO {
    @NotBlank(message = "闂灞炴€т笉鑳戒负绌?")
    private String issueFunctionCode;
    private String remark;
}
