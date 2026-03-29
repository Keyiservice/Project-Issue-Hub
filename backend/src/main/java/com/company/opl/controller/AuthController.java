package com.company.opl.controller;

import com.company.opl.common.Result;
import com.company.opl.dto.ChangePasswordRequest;
import com.company.opl.dto.LoginRequest;
import com.company.opl.dto.MiniappBindRequest;
import com.company.opl.dto.MiniappLoginRequest;
import com.company.opl.service.AuthService;
import com.company.opl.vo.CurrentUserVO;
import com.company.opl.vo.LoginVO;
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
    @Operation(summary = "账号密码登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/miniapp-login")
    @Operation(summary = "微信小程序登录")
    public Result<LoginVO> miniappLogin(@Valid @RequestBody MiniappLoginRequest request) {
        return Result.success(authService.miniappLogin(request));
    }

    @PostMapping("/miniapp-bind")
    @Operation(summary = "微信小程序首次绑定企业账号")
    public Result<LoginVO> miniappBind(@Valid @RequestBody MiniappBindRequest request) {
        return Result.success(authService.bindMiniapp(request));
    }

    @PostMapping("/change-password")
    @Operation(summary = "修改密码")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return Result.success(null);
    }

    @GetMapping("/me")
    @Operation(summary = "当前登录人信息")
    public Result<CurrentUserVO> me() {
        return Result.success(authService.getCurrentUser());
    }
}
