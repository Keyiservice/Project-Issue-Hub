package com.company.opl.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MiniappBindRequest {

    @NotBlank(message = "微信登录凭证不能为空")
    private String code;

    private String devOpenId;

    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
