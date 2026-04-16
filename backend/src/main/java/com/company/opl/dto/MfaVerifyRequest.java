package com.company.opl.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MfaVerifyRequest {
    private String mfaToken;

    @NotBlank(message = "MFA code is required")
    private String code;
}
