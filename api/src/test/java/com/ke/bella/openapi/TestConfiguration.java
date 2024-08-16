package com.ke.bella.openapi;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.ke.bella.openapi.configuration.OpenapiBeanNameGenerator;
import com.lianjia.hawk.config.HawkAutoConfigure;
import com.lianjia.hawk.config.HawkServletFilterAutoConfigure;
import com.lianjia.hawk.config.HawkWatchAutoConfigure;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Author: Stan Sai Date: 2024/8/8 17:39 description:
 */
@SpringBootApplication(exclude = { DataSourceTransactionManagerAutoConfiguration.class, HawkAutoConfigure.class,
        HawkWatchAutoConfigure.class, HawkServletFilterAutoConfigure.class,
        HawkServletFilterAutoConfigure.class })
@ComponentScan(basePackages = { "com.ke.bella.openapi" }, nameGenerator = OpenapiBeanNameGenerator.class)
@EnableApolloConfig
public class TestConfiguration {
}
