package com.ke.bella.openapi;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 服务启动类
 *
 * @author keboot
 */
@EnableApolloConfig
@SpringCloudApplication
@ComponentScan({ "com.ke.bella.openapi" })
@Slf4j
@EnableConfigurationProperties
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        LOGGER.info("启动成功");
    }
}
