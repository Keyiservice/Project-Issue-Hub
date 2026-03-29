package com.company.opl.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MiniappLoginRequest {

    @NotBlank(message = "微信登录凭证不能为空")
    private String code;

    private String devOpenId;
}
