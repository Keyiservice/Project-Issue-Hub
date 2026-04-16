package com.company.opl.service;

import com.company.opl.dto.ChangePasswordRequest;
import com.company.opl.dto.LoginRequest;
import com.company.opl.dto.MfaSetupRequest;
import com.company.opl.dto.MfaVerifyRequest;
import com.company.opl.dto.MiniappBindRequest;
import com.company.opl.dto.MiniappLoginRequest;
import com.company.opl.vo.CurrentUserVO;
import com.company.opl.vo.LoginVO;

public interface AuthService {

    LoginVO login(LoginRequest request);

    LoginVO miniappLogin(MiniappLoginRequest request);

    LoginVO bindMiniapp(MiniappBindRequest request);

    void changePassword(ChangePasswordRequest request);

    com.company.opl.vo.MfaSetupVO prepareMfa(MfaSetupRequest request);

    LoginVO verifyMfa(MfaVerifyRequest request);

    CurrentUserVO getCurrentUser();
}
