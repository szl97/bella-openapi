package com.ke.bella.openapi.login.config;

import com.ke.bella.openapi.Operator;
import com.ke.bella.openapi.login.cas.BellaAuthenticationFilter;
import com.ke.bella.openapi.login.cas.BellaCasClient;
import com.ke.bella.openapi.login.cas.BellaRedirectFilter;
import com.ke.bella.openapi.login.cas.BellaValidatorFilter;
import com.ke.bella.openapi.login.cas.CasProperties;
import com.ke.bella.openapi.login.session.SessionManager;
import com.ke.bella.openapi.login.session.SessionProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class BellaLoginConfiguration {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addExposedHeader("X-Redirect-Login");
        config.addExposedHeader("Location");
        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
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
