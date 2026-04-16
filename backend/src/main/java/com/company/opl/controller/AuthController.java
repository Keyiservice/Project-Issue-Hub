package com.company.opl.controller;

import com.company.opl.common.Result;
import com.company.opl.dto.ChangePasswordRequest;
import com.company.opl.dto.LoginRequest;
import com.company.opl.dto.MfaSetupRequest;
import com.company.opl.dto.MfaVerifyRequest;
import com.company.opl.dto.MiniappBindRequest;
import com.company.opl.dto.MiniappLoginRequest;
import com.company.opl.service.AuthService;
import com.company.opl.vo.CurrentUserVO;
import com.company.opl.vo.LoginVO;
import com.company.opl.vo.MfaSetupVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Username & password login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/miniapp-login")
    @Operation(summary = "Miniapp login")
    public Result<LoginVO> miniappLogin(@Valid @RequestBody MiniappLoginRequest request) {
        return Result.success(authService.miniappLogin(request));
    }

    @PostMapping("/miniapp-bind")
    @Operation(summary = "Miniapp first bind to enterprise account")
    public Result<LoginVO> miniappBind(@Valid @RequestBody MiniappBindRequest request) {
        return Result.success(authService.bindMiniapp(request));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return Result.success(null);
    }

    @PostMapping("/mfa/setup")
    @Operation(summary = "MFA setup (TOTP secret)")
    public Result<MfaSetupVO> setupMfa(@RequestBody MfaSetupRequest request) {
        return Result.success(authService.prepareMfa(request));
    }

    @PostMapping("/mfa/verify")
    @Operation(summary = "MFA verify and issue token")
    public Result<LoginVO> verifyMfa(@Valid @RequestBody MfaVerifyRequest request) {
        return Result.success(authService.verifyMfa(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Current user info")
    public Result<CurrentUserVO> me() {
        return Result.success(authService.getCurrentUser());
    }
}
