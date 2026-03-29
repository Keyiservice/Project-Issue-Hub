package com.company.opl.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UserImportItemDTO {

    @NotBlank(message = "账号不能为空")
    @Size(max = 64, message = "账号长度不能超过 64 位")
    private String username;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 64, message = "姓名长度不能超过 64 位")
    private String realName;

    @Size(max = 64, message = "部门编码长度不能超过 64 位")
    private String departmentCode;

    @Size(max = 128, message = "部门名称长度不能超过 128 位")
    private String departmentName;

    private List<String> roleCodes;

    @Size(max = 20, message = "手机号长度不能超过 20 位")
    private String mobile;

    @Size(max = 128, message = "邮箱长度不能超过 128 位")
    private String email;
}
