package com.ke.bella.openapi.login.config;

import com.ke.bella.openapi.Operator;
import com.ke.bella.openapi.login.cas.BellaAuthenticationFilter;
import com.ke.bella.openapi.login.cas.BellaCasClient;
import com.ke.bella.openapi.login.cas.BellaRedirectFilter;
import com.ke.bella.openapi.login.cas.BellaValidatorFilter;
import com.ke.bella.openapi.login.cas.CasProperties;
import com.ke.bella.openapi.login.cors.CORSFilter;
import com.ke.bella.openapi.login.session.SessionManager;
import com.ke.bella.openapi.login.session.SessionProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class BellaLoginConfiguration {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Bean
    public FilterRegistrationBean<CORSFilter> corsFilterFilter() {
        FilterRegistrationBean<CORSFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new CORSFilter());
        filterRegistrationBean.setOrder(CORSFilter.ORDER);
        return filterRegistrationBean;
    }

    private RedisTemplate<String, Operator> redisTemplate() {
        RedisTemplate<String, Operator> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setDefaultSerializer(new JdkSerializationRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    @ConfigurationProperties(value = "bella.session")
    public SessionProperty sessionProperty() {
        return new SessionProperty();
    }

    @Bean
    public SessionManager sessionManager(SessionProperty sessionProperty) {
        return new SessionManager(sessionProperty, redisTemplate());
    }

    @Bean
    @ConfigurationProperties(value = "bella.cas")
    public CasProperties casProperties() {
        return new CasProperties();
    }

    @Bean
    @ConditionalOnCasEnable
    public BellaCasClient bellaCasClient(CasProperties casProperties, SessionManager sessionManager) {
        return new BellaCasClient(casProperties, sessionManager);
    }

    @Bean
    @ConditionalOnCasEnable
    public FilterRegistrationBean<BellaValidatorFilter> casValidationFilter(BellaCasClient bellaCasClient) {
        return bellaCasClient.casValidationFilter();
    }

    @Bean
    @ConditionalOnCasEnable
    public FilterRegistrationBean<BellaRedirectFilter> casRedirectFilter(BellaCasClient bellaCasClient) {
        return bellaCasClient.casRedirectRegistrationBean();
    }

    @Bean
    @ConditionalOnCasEnable
    public FilterRegistrationBean<BellaAuthenticationFilter> casAuthenticationFilter(BellaCasClient bellaCasClient) {
        return bellaCasClient.casAuthenticationFilter();
    }
}
