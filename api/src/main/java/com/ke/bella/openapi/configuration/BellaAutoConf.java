package com.ke.bella.openapi.configuration;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ke.bella.openapi.protocol.AdaptorManager;
import com.ke.bella.openapi.protocol.IProtocolAdaptor;

@Configuration
public class BellaAutoConf {

    @Bean
    public AdaptorManager adaptorManager(@Autowired Map<String, IProtocolAdaptor> adaptors) {
        AdaptorManager manager = new AdaptorManager();
        adaptors.forEach(manager::register);
        return manager;
    }
}
