package com.ke.bella.openapi.configuration;

import com.ke.bella.openapi.common.EntityConstants;
import com.ke.bella.openapi.intercept.AuthorizationInterceptor;
import com.ke.bella.openapi.intercept.ConcurrentStartInterceptor;
import com.ke.bella.openapi.intercept.MockEndpointInterceptor;
import com.ke.bella.openapi.intercept.MonthQuotaInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    public static final List<String> endpointPathPatterns = Arrays.stream(EntityConstants.SystemBasicEndpoint.values())
            .map(EntityConstants.SystemBasicEndpoint::getEndpoint).collect(Collectors.toList());
    @Autowired
    private ConcurrentStartInterceptor concurrentStartInterceptor;
    @Autowired
    private AuthorizationInterceptor authorizationInterceptor;
    @Autowired
    private MonthQuotaInterceptor monthQuotaInterceptor;
    @Autowired
    private MockEndpointInterceptor mockEndpointInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authorizationInterceptor)
                .addPathPatterns("/console/**")
                .addPathPatterns("/v*/**")
                .order(100);
        registry.addInterceptor(monthQuotaInterceptor)
                .addPathPatterns(endpointPathPatterns)
                .order(110);
        registry.addInterceptor(concurrentStartInterceptor);
        registry.addInterceptor(mockEndpointInterceptor)
                .addPathPatterns(endpointPathPatterns);
    }
}
