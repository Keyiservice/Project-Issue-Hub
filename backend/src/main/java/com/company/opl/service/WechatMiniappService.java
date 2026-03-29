package com.company.opl.service;

public interface WechatMiniappService {

    MiniappSession resolveSession(String code, String devOpenId);

    record MiniappSession(String openId, String unionId) {
    }
}
