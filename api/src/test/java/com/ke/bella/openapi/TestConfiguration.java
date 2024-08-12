package com.ke.bella.openapi;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.lianjia.hawk.config.HawkAutoConfigure;
import com.lianjia.hawk.config.HawkServletFilterAutoConfigure;
import com.lianjia.hawk.config.HawkWatchAutoConfigure;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;

/**
 * Author: Stan Sai Date: 2024/8/8 17:39 description:
 */
@SpringBootApplication(exclude = { DataSourceTransactionManagerAutoConfiguration.class, HawkAutoConfigure.class,
        HawkWatchAutoConfigure.class, HawkServletFilterAutoConfigure.class,
        HawkServletFilterAutoConfigure.class }, scanBasePackages = { "com.ke.bella.openapi" })
@EnableApolloConfig
public class TestConfiguration {
}
