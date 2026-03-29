package com.company.opl.dto.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UserBatchImportDTO {

    @NotEmpty(message = "导入列表不能为空")
    @Valid
    private List<UserImportItemDTO> items;

    @Size(min = 6, max = 32, message = "初始密码长度必须在 6-32 位之间")
    private String initialPassword;
}
