package com.ke.bella.openapi;

import com.alicp.jetcache.anno.config.EnableMethodCache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;

@EnableApolloConfig
@ComponentScan(basePackages = { "com.ke.bella.openapi" })
@SpringBootApplication
@EnableMethodCache(basePackages = "com.ke.bella.openapi")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
