package com.company.opl.dto.dict;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DictItemSaveDTO {

    @NotBlank(message = "显示名称不能为空")
    @Size(max = 64, message = "显示名称不能超过64个字符")
    private String itemLabel;

    @NotBlank(message = "字典值不能为空")
    @Size(max = 64, message = "字典值不能超过64个字符")
    private String itemValue;

    @Size(max = 32, message = "颜色不能超过32个字符")
    private String itemColor;

    @NotNull(message = "排序不能为空")
    private Integer sortNo;

    @NotNull(message = "默认标记不能为空")
    private Boolean isDefault;

    @NotBlank(message = "状态不能为空")
    private String status;

    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
