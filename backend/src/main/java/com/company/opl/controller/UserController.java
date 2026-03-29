package com.company.opl.controller;

import com.company.opl.common.PageResult;
import com.company.opl.common.Result;
import com.company.opl.dto.user.UserBatchImportDTO;
import com.company.opl.dto.user.UserCreateDTO;
import com.company.opl.dto.user.UserUpdateDTO;
import com.company.opl.query.user.UserQuery;
import com.company.opl.service.UserService;
import com.company.opl.vo.SimpleUserVO;
import com.company.opl.vo.user.RoleOptionVO;
import com.company.opl.vo.user.UserAdminVO;
import com.company.opl.vo.user.UserImportResultVO;
import com.company.opl.vo.user.UserProjectParticipationVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "用户分页列表")
    public Result<PageResult<UserAdminVO>> page(UserQuery query) {
        return Result.success(userService.pageUsers(query));
    }

    @GetMapping("/options")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER','ADMIN','RESP_ENGINEER','SITE_USER')")
    @Operation(summary = "启用用户下拉列表")
    public Result<List<SimpleUserVO>> options() {
        return Result.success(userService.listEnabledUsers());
    }

    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "角色选项")
    public Result<List<RoleOptionVO>> roles() {
        return Result.success(userService.listRoleOptions());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建用户")
    public Result<UserAdminVO> create(@Valid @RequestBody UserCreateDTO request) {
        return Result.success(userService.createUser(request));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新用户")
    public Result<UserAdminVO> update(@PathVariable Long userId, @Valid @RequestBody UserUpdateDTO request) {
        return Result.success(userService.updateUser(userId, request));
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "批量导入用户")
    public Result<UserImportResultVO> importUsers(@Valid @RequestBody UserBatchImportDTO request) {
        return Result.success(userService.importUsers(request));
    }

    @GetMapping("/{userId}/projects")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "用户参与项目")
    public Result<List<UserProjectParticipationVO>> projects(@PathVariable Long userId) {
        return Result.success(userService.listUserProjects(userId));
    }

    @PutMapping("/{userId}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "重置用户密码")
    public Result<Void> resetPassword(@PathVariable("userId") Long userId) {
        userService.resetPassword(userId);
        return Result.success(null);
    }

    @PutMapping("/{userId}/unbind-wechat")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "解绑微信")
    public Result<Void> unbindWechat(@PathVariable("userId") Long userId) {
        userService.unbindWechat(userId);
        return Result.success(null);
    }
}
