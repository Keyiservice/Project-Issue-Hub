package com.company.opl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.opl.config.MfaProperties;
import com.company.opl.dto.ChangePasswordRequest;
import com.company.opl.dto.LoginRequest;
import com.company.opl.dto.MfaSetupRequest;
import com.company.opl.dto.MfaVerifyRequest;
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
import com.company.opl.util.MfaTotpUtils;
import com.company.opl.vo.CurrentUserVO;
import com.company.opl.vo.LoginVO;
import com.company.opl.vo.MfaSetupVO;
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
    private final MfaProperties mfaProperties;

    public AuthServiceImpl(SysUserMapper sysUserMapper,
                           UserService userService,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           JwtProperties jwtProperties,
                           WechatMiniappService wechatMiniappService,
                           MfaProperties mfaProperties) {
        this.sysUserMapper = sysUserMapper;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
        this.wechatMiniappService = wechatMiniappService;
        this.mfaProperties = mfaProperties;
    }

    @Override
    public LoginVO login(LoginRequest request) {
        SysUser user = loadUserByUsername(request.getUsername());
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("Invalid username or password");
        }
        validateUserStatus(user);
        if (isTrue(user.getPasswordChangeRequired())) {
            LoginVO result = buildLoginResult(user, true);
            result.setPasswordChangeRequired(true);
            return result;
        }
        return handleMfaOrLogin(user, request.getMfaCode());
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
            throw new BusinessException("Wechat already bound to another account");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("Invalid username or password");
        }
        validateUserStatus(user);
        if (StringUtils.hasText(user.getWechatOpenid()) && !session.openId().equals(user.getWechatOpenid())) {
            throw new BusinessException("Account already bound to another Wechat");
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
            throw new BusinessException("New password confirmation does not match");
        }
        SysUser user = getCurrentUserEntity();
        boolean forceChange = isTrue(user.getPasswordChangeRequired());
        if (!forceChange) {
            if (!StringUtils.hasText(request.getOldPassword())) {
                throw new BusinessException("Old password is required");
            }
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
                throw new BusinessException("Old password is incorrect");
            }
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException("New password must be different from old password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangeRequired(0);
        user.setPasswordChangedAt(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }

    @Override
    public MfaSetupVO prepareMfa(MfaSetupRequest request) {
        SysUser user = resolveUserForMfa(request.getMfaToken());
        String secret = user.getMfaSecret();
        if (!StringUtils.hasText(secret)) {
            secret = MfaTotpUtils.generateSecret();
            user.setMfaSecret(secret);
            sysUserMapper.updateById(user);
        }
        String issuer = mfaProperties.getIssuer();
        String account = user.getUsername();
        String otpauth = MfaTotpUtils.buildOtpAuthUrl(
                issuer, account, secret, mfaProperties.getDigits(), mfaProperties.getStepSeconds());
        return MfaSetupVO.builder()
                .issuer(issuer)
                .account(account)
                .secret(secret)
                .otpauthUrl(otpauth)
                .build();
    }

    @Override
    public LoginVO verifyMfa(MfaVerifyRequest request) {
        SysUser user = resolveUserForMfa(request.getMfaToken());
        if (!StringUtils.hasText(user.getMfaSecret())) {
            throw new BusinessException("MFA is not initialized");
        }
        boolean ok = MfaTotpUtils.verifyCode(
                user.getMfaSecret(),
                request.getCode(),
                mfaProperties.getDigits(),
                mfaProperties.getStepSeconds(),
                mfaProperties.getWindow()
        );
        if (!ok) {
            throw new BusinessException("Invalid MFA code");
        }
        user.setMfaEnabled(1);
        user.setMfaVerifiedAt(LocalDateTime.now());
        sysUserMapper.updateById(user);
        return buildLoginResult(user, true);
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
                .mfaEnabled(isTrue(user.getMfaEnabled()))
                .roles(roles)
                .build();
    }

    private SysUser getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            throw new BusinessException("Not authenticated");
        }
        SysUser user = sysUserMapper.selectById(loginUser.getUserId());
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("User not found");
        }
        validateUserStatus(user);
        return user;
    }

    private SysUser loadUserByUsername(String username) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .last("limit 1"));
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("User not found");
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
            throw new BusinessException("Account is disabled");
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
                .mfaRequired(false)
                .mfaSetupRequired(false)
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

    private LoginVO handleMfaOrLogin(SysUser user, String mfaCode) {
        if (!mfaProperties.isRequired()) {
            return buildLoginResult(user, true);
        }
        if (!isTrue(user.getMfaEnabled())) {
            String token = jwtTokenProvider.generateMfaToken(user.getId(), user.getUsername(), 300);
            return LoginVO.builder()
                    .mfaSetupRequired(true)
                    .mfaRequired(false)
                    .mfaToken(token)
                    .bound(true)
                    .wechatBound(StringUtils.hasText(user.getWechatOpenid()))
                    .passwordChangeRequired(false)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .realName(user.getRealName())
                    .departmentCode(user.getDepartmentCode())
                    .departmentName(user.getDepartmentName())
                    .roles(userService.getUserRoleCodes(user.getId()))
                    .build();
        }
        if (!StringUtils.hasText(mfaCode)) {
            String token = jwtTokenProvider.generateMfaToken(user.getId(), user.getUsername(), 300);
            return LoginVO.builder()
                    .mfaRequired(true)
                    .mfaSetupRequired(false)
                    .mfaToken(token)
                    .bound(true)
                    .wechatBound(StringUtils.hasText(user.getWechatOpenid()))
                    .passwordChangeRequired(false)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .realName(user.getRealName())
                    .departmentCode(user.getDepartmentCode())
                    .departmentName(user.getDepartmentName())
                    .roles(userService.getUserRoleCodes(user.getId()))
                    .build();
        }
        if (!StringUtils.hasText(user.getMfaSecret())) {
            throw new BusinessException("MFA is not initialized");
        }
        boolean ok = MfaTotpUtils.verifyCode(
                user.getMfaSecret(),
                mfaCode,
                mfaProperties.getDigits(),
                mfaProperties.getStepSeconds(),
                mfaProperties.getWindow()
        );
        if (!ok) {
            throw new BusinessException("Invalid MFA code");
        }
        return buildLoginResult(user, true);
    }

    private SysUser resolveUserForMfa(String mfaToken) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            SysUser user = sysUserMapper.selectById(loginUser.getUserId());
            if (user != null) {
                return user;
            }
        }
        if (!StringUtils.hasText(mfaToken)) {
            throw new BusinessException("MFA token is required");
        }
        try {
            io.jsonwebtoken.Claims claims = jwtTokenProvider.parseClaims(mfaToken);
            Boolean pending = claims.get("mfaPending", Boolean.class);
            if (!Boolean.TRUE.equals(pending)) {
                throw new BusinessException("Invalid MFA token");
            }
            Long userId = claims.get("userId", Long.class);
            SysUser user = sysUserMapper.selectById(userId);
            if (user == null || user.getDeleted() == 1) {
                throw new BusinessException("User not found");
            }
            validateUserStatus(user);
            return user;
        } catch (Exception ex) {
            if (ex instanceof BusinessException) {
                throw ex;
            }
            throw new BusinessException("Invalid MFA token");
        }
    }
}
