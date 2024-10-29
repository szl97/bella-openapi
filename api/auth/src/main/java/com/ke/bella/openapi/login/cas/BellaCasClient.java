package com.ke.bella.openapi.login.cas;

import com.ke.bella.openapi.login.cors.CORSFilter;
import com.ke.bella.openapi.login.session.SessionManager;
import org.jasig.cas.client.configuration.ConfigurationKeys;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import java.util.HashMap;
import java.util.Map;

public class BellaCasClient {
    public static final String redirectParameter = "redirect";
    private static final int CAS_STEP_ONE = CORSFilter.ORDER + 1;
    private static final int CAS_STEP_TWO = CAS_STEP_ONE + 1;
    private static final int CAS_STEP_THREE = CAS_STEP_TWO + 1;

    private final CasProperties casProperties;

    private final SessionManager sessionManager;

    public BellaCasClient(CasProperties casProperties, SessionManager sessionManager) {
        this.casProperties = casProperties;
        this.sessionManager = sessionManager;
    }

    public FilterRegistrationBean<BellaValidatorFilter> casValidationFilter() {
        FilterRegistrationBean<BellaValidatorFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        BellaValidatorFilter targetCasValidationFilter = new BellaValidatorFilter(casProperties.getIdAttribute(),
                casProperties.getNameAttribute(), sessionManager);
        filterRegistrationBean.setFilter(targetCasValidationFilter);
        filterRegistrationBean.setOrder(CAS_STEP_ONE);
        Map<String, String> initParams = new HashMap<>();
        initParams.put(ConfigurationKeys.CAS_SERVER_URL_PREFIX.getName(), casProperties.getServerUrlPrefix());
        initParams.put(ConfigurationKeys.SERVER_NAME.getName(), casProperties.getClientHost());
        initParams.put(ConfigurationKeys.REDIRECT_AFTER_VALIDATION.getName(), String.valueOf(Boolean.FALSE));
        filterRegistrationBean.setInitParameters(initParams);
        filterRegistrationBean.setUrlPatterns(casProperties.getValidationUrlPatterns());
        filterRegistrationBean.getInitParameters().put(ConfigurationKeys.USE_SESSION.getName(), String.valueOf(Boolean.TRUE));
        return filterRegistrationBean;
    }

    public FilterRegistrationBean<BellaRedirectFilter> casRedirectRegistrationBean() {
        FilterRegistrationBean<BellaRedirectFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        BellaRedirectFilter targetCasAuthFilter = new BellaRedirectFilter();
        filterRegistrationBean.setFilter(targetCasAuthFilter);
        filterRegistrationBean.setOrder(CAS_STEP_TWO);
        filterRegistrationBean.setUrlPatterns(casProperties.getValidationUrlPatterns());
        return filterRegistrationBean;
    }

    public FilterRegistrationBean<BellaAuthenticationFilter> casAuthenticationFilter() {
        FilterRegistrationBean<BellaAuthenticationFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        BellaAuthenticationFilter filter = new BellaAuthenticationFilter(casProperties.getServerLoginUrl(), casProperties.getClientHost(),
                casProperties.getClientUri(), casProperties.isClientSupport(), casProperties.getClientIndexUrl(),
                casProperties.getLogoutUri(), sessionManager);
        filterRegistrationBean.setOrder(CAS_STEP_THREE);
        filterRegistrationBean.setFilter(filter);
        return filterRegistrationBean;
    }
}
