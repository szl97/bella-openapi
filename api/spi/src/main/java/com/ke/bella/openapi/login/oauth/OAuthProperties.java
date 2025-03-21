package com.ke.bella.openapi.login.oauth;

import com.google.common.collect.Lists;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@Data
public class OAuthProperties {
    private String loginPageUrl; // 统一的OAuth登录页面URL
    private String authorizationHeader; // 用于跳过认证的header
    private Map<String, ProviderConfig> providers; // 支持的OAuth提供商配置
    private List<String> validationUrlPatterns = Lists.newArrayList("/console/*");
    private String clientIndex;

    @Data
    public static class ProviderConfig {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String scope;
        private String authUri;
        private String tokenUri;
        private String userInfoUri;
        private String[] allowedRedirectDomains;
        private boolean enabled = true;
    }
}
