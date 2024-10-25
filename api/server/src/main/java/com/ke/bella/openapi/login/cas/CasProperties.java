package com.ke.bella.openapi.login.cas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CasProperties {
    private String serverUrlPrefix;
    private String serverLoginUrl;
    private String clientHost;
    private String clientUri;
    private String clientIndexUrl;
    private String logoutUri;
    private List<String> validationUrlPatterns;
    private boolean clientSupport;
    private String idAttribute = "ucid";
    private String nameAttribute = "displayName";
}

