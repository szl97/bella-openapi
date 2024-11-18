package com.ke.bella.openapi;

import com.alicp.jetcache.anno.config.EnableMethodCache;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.lianjia.hawk.config.HawkAutoConfigure;
import com.lianjia.hawk.config.HawkServletFilterAutoConfigure;
import com.lianjia.hawk.config.HawkWatchAutoConfigure;

@SpringBootApplication(exclude = { DataSourceTransactionManagerAutoConfiguration.class, HawkAutoConfigure.class,
        HawkWatchAutoConfigure.class, HawkServletFilterAutoConfigure.class,
        HawkServletFilterAutoConfigure.class })
@ComponentScan(basePackages = { "com.ke.bella.openapi" })
@EnableConfigurationProperties
@EnableApolloConfig
@EnableMethodCache(basePackages = "com.ke.bella.openapi")
public class TestConfiguration {

}
