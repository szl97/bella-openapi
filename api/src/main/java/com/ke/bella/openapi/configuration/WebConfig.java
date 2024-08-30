package com.ke.bella.openapi.configuration;

import com.ke.bella.openapi.db.TableConstants;
import com.ke.bella.openapi.intercept.AuthorizationInterceptor;

import com.ke.bella.openapi.intercept.ConcurrentStartInterceptor;
import com.ke.bella.openapi.intercept.MonthQuotaInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    public static final List<String> endpointPathPatterns = Arrays.stream(TableConstants.SystemBasicEndpoint.values())
            .map(TableConstants.SystemBasicEndpoint::getEndpoint).collect(Collectors.toList());
    @Autowired
    private ConcurrentStartInterceptor concurrentStartInterceptor;
    @Autowired
    private AuthorizationInterceptor authorizationInterceptor;
    @Autowired
    private MonthQuotaInterceptor monthQuotaInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authorizationInterceptor)
                .addPathPatterns("/console/**")
                .addPathPatterns(endpointPathPatterns)
                .addPathPatterns("/v*/meta/auth/**")
                .addPathPatterns("/v*/apikey/**")
                .order(100);
        registry.addInterceptor(monthQuotaInterceptor)
                .addPathPatterns(endpointPathPatterns)
                .order(110);
        registry.addInterceptor(concurrentStartInterceptor);
    }
}
