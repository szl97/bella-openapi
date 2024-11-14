package com.ke.bella.openapi.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "openapi")
@Data
public class OpenApiProperties {
    private List<String> loginRoles = new ArrayList<>();
    private List<String> loginExcludes = new ArrayList<>();
    private Map<Long, String> managers = new HashMap<>();
}
