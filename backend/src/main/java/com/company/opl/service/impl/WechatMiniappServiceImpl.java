package com.company.opl.service.impl;

import com.company.opl.config.WechatMiniappProperties;
import com.company.opl.exception.BusinessException;
import com.company.opl.service.WechatMiniappService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
public class WechatMiniappServiceImpl implements WechatMiniappService {

    private final WechatMiniappProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public WechatMiniappServiceImpl(WechatMiniappProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.weixin.qq.com")
                .build();
    }

    @Override
    public MiniappSession resolveSession(String code, String devOpenId) {
        if (StringUtils.hasText(properties.getAppId()) && StringUtils.hasText(properties.getSecret())) {
            return resolveFromWechat(code);
        }
        if (properties.isMockEnabled() && StringUtils.hasText(devOpenId)) {
            return new MiniappSession(devOpenId.trim(), null);
        }
        throw new BusinessException("未配置微信小程序 AppID/Secret，当前环境无法完成微信绑定");
    }

    private MiniappSession resolveFromWechat(String code) {
        if (!StringUtils.hasText(code)) {
            throw new BusinessException("微信登录凭证不能为空");
        }
        try {
            String response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/sns/jscode2session")
                            .queryParam("appid", properties.getAppId())
                            .queryParam("secret", properties.getSecret())
                            .queryParam("js_code", code)
                            .queryParam("grant_type", "authorization_code")
                            .build())
                    .retrieve()
                    .body(String.class);
            JsonNode root = objectMapper.readTree(response);
            int errorCode = root.path("errcode").asInt(0);
            if (errorCode != 0) {
                throw new BusinessException("微信登录失败: " + root.path("errmsg").asText("code2session error"));
            }
            String openId = root.path("openid").asText(null);
            if (!StringUtils.hasText(openId)) {
                throw new BusinessException("微信登录失败，未获取到 openid");
            }
            String unionId = root.path("unionid").asText(null);
            return new MiniappSession(openId, unionId);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("微信登录失败，请稍后重试");
        }
    }
}
