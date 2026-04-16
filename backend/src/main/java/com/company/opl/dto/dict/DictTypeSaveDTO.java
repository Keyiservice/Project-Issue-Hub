package com.company.opl.dto.dict;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DictTypeSaveDTO {

    @NotBlank(message = "字典编码不能为空")
    @Size(max = 64, message = "字典编码不能超过64个字符")
    private String dictCode;

    @NotBlank(message = "字典名称不能为空")
    @Size(max = 64, message = "字典名称不能超过64个字符")
    private String dictName;

    @NotBlank(message = "状态不能为空")
    private String status;

    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
