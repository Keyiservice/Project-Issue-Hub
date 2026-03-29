package com.company.opl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.opl.dto.ChangePasswordRequest;
import com.company.opl.dto.LoginRequest;
import com.company.opl.dto.MiniappBindRequest;
import com.company.opl.dto.MiniappLoginRequest;
import com.company.opl.entity.SysUser;
import com.company.opl.enums.UserStatusEnum;
import com.company.opl.exception.BusinessException;
import com.company.opl.mapper.SysUserMapper;
import com.company.opl.security.JwtProperties;
import com.company.opl.security.JwtTokenProvider;
import com.company.opl.security.LoginUser;
import com.company.opl.service.AuthService;
import com.company.opl.service.UserService;
import com.company.opl.service.WechatMiniappService;
import com.company.opl.vo.CurrentUserVO;
import com.company.opl.vo.LoginVO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final WechatMiniappService wechatMiniappService;

    public AuthServiceImpl(SysUserMapper sysUserMapper,
                           UserService userService,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           JwtProperties jwtProperties,
                           WechatMiniappService wechatMiniappService) {
        this.sysUserMapper = sysUserMapper;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
        this.wechatMiniappService = wechatMiniappService;
    }

    @Override
    public LoginVO login(LoginRequest request) {
        SysUser user = loadUserByUsername(request.getUsername());
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("用户名或密码错误");
        }
        validateUserStatus(user);
        return buildLoginResult(user, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO miniappLogin(MiniappLoginRequest request) {
        WechatMiniappService.MiniappSession session =
                wechatMiniappService.resolveSession(request.getCode(), request.getDevOpenId());
        SysUser user = loadUserByOpenId(session.openId());
        if (user == null) {
            return LoginVO.builder()
                    .bound(false)
                    .wechatBound(false)
                    .passwordChangeRequired(false)
                    .build();
        }
        validateUserStatus(user);
        syncWechatIdentity(user, session);
        return buildLoginResult(user, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO bindMiniapp(MiniappBindRequest request) {
        WechatMiniappService.MiniappSession session =
                wechatMiniappService.resolveSession(request.getCode(), request.getDevOpenId());
        SysUser existingWechatUser = loadUserByOpenId(session.openId());
        SysUser user = loadUserByUsername(request.getUsername());

        if (existingWechatUser != null && !existingWechatUser.getId().equals(user.getId())) {
            throw new BusinessException("当前微信已绑定其他账号，请联系管理员处理");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("账号或密码错误");
        }
        validateUserStatus(user);
        if (StringUtils.hasText(user.getWechatOpenid()) && !session.openId().equals(user.getWechatOpenid())) {
            throw new BusinessException("该账号已绑定其他微信，请联系管理员解绑后再重试");
        }

        user.setWechatOpenid(session.openId());
        if (StringUtils.hasText(session.unionId())) {
            user.setWechatUnionid(session.unionId());
        }
        user.setWechatBoundAt(LocalDateTime.now());
        sysUserMapper.updateById(user);

        return buildLoginResult(user, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的新密码不一致");
        }
        SysUser user = getCurrentUserEntity();
        boolean forceChange = isTrue(user.getPasswordChangeRequired());
        if (!forceChange) {
            if (!StringUtils.hasText(request.getOldPassword())) {
                throw new BusinessException("请输入旧密码");
            }
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
                throw new BusinessException("旧密码错误");
            }
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangeRequired(0);
        user.setPasswordChangedAt(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }

    @Override
    public CurrentUserVO getCurrentUser() {
        SysUser user = getCurrentUserEntity();
        List<String> roles = userService.getUserRoleCodes(user.getId());
        return CurrentUserVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .departmentCode(user.getDepartmentCode())
                .departmentName(user.getDepartmentName())
                .wechatBound(StringUtils.hasText(user.getWechatOpenid()))
                .passwordChangeRequired(isTrue(user.getPasswordChangeRequired()))
                .roles(roles)
                .build();
    }

    private SysUser getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            throw new BusinessException("未登录");
        }
        SysUser user = sysUserMapper.selectById(loginUser.getUserId());
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("当前用户不存在");
        }
        validateUserStatus(user);
        return user;
    }

    private SysUser loadUserByUsername(String username) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .last("limit 1"));
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private SysUser loadUserByOpenId(String openId) {
        return sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getWechatOpenid, openId)
                .last("limit 1"));
    }

    private void validateUserStatus(SysUser user) {
        if (!UserStatusEnum.ENABLED.name().equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用或锁定");
        }
    }

    private void syncWechatIdentity(SysUser user, WechatMiniappService.MiniappSession session) {
        boolean changed = false;
        if (!StringUtils.hasText(user.getWechatOpenid())) {
            user.setWechatOpenid(session.openId());
            changed = true;
        }
        if (StringUtils.hasText(session.unionId()) && !session.unionId().equals(user.getWechatUnionid())) {
            user.setWechatUnionid(session.unionId());
            changed = true;
        }
        if (user.getWechatBoundAt() == null) {
            user.setWechatBoundAt(LocalDateTime.now());
            changed = true;
        }
        if (changed) {
            sysUserMapper.updateById(user);
        }
    }

    private LoginVO buildLoginResult(SysUser user, boolean bound) {
        List<String> roles = userService.getUserRoleCodes(user.getId());
        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getDepartmentCode(),
                user.getDepartmentName(),
                roles
        );
        user.setLastLoginAt(LocalDateTime.now());
        sysUserMapper.updateById(user);
        return LoginVO.builder()
                .bound(bound)
                .wechatBound(StringUtils.hasText(user.getWechatOpenid()))
                .passwordChangeRequired(isTrue(user.getPasswordChangeRequired()))
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .departmentCode(user.getDepartmentCode())
                .departmentName(user.getDepartmentName())
                .roles(roles)
                .accessToken(token)
                .tokenType("Bearer")
                .expireInSeconds(jwtProperties.getExpireMinutes() * 60)
                .build();
    }

    private boolean isTrue(Integer value) {
        return value != null && value == 1;
    }
}
