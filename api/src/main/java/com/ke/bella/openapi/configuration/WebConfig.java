package com.ke.bella.openapi.configuration;

import com.ke.bella.openapi.db.TableConstants;
import com.ke.bella.openapi.intercept.AuthorizationInterceptor;
import com.ke.bella.openapi.intercept.ManagerInterceptor;

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
    private AuthorizationInterceptor authorizationInterceptor;
    @Autowired
    private ManagerInterceptor managerInterceptor;

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
                .order(100);
        registry.addInterceptor(managerInterceptor)
                .addPathPatterns("/console/**")
                .order(200);
    }
}
