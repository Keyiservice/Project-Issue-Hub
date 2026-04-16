package com.company.opl.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MfaSetupVO {
    private String issuer;
    private String account;
    private String secret;
    private String otpauthUrl;
}
