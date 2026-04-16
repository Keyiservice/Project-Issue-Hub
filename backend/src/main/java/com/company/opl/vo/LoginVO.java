package com.company.opl.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LoginVO {
    private Boolean bound;
    private Boolean wechatBound;
    private Boolean passwordChangeRequired;
    private Boolean mfaRequired;
    private Boolean mfaSetupRequired;
    private String mfaToken;
    private Long userId;
    private String username;
    private String realName;
    private String departmentCode;
    private String departmentName;
    private List<String> roles;
    private String accessToken;
    private String tokenType;
    private Long expireInSeconds;
}
