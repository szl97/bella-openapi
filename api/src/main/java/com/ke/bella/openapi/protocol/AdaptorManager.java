package com.ke.bella.openapi.protocol;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class AdaptorManager {
    private Map<String, Map<String, IProtocolAdaptor>> adaptors;

    public AdaptorManager(@Autowired Map<String, IProtocolAdaptor> adaptors) {
        this.adaptors = new HashMap<>();
        adaptors.forEach((key, value) -> {
            String endpoint = value.endpoint();
            this.adaptors.putIfAbsent(endpoint, new HashMap<>());
            String protocol = key.split("-")[0];
            this.adaptors.get(endpoint).put(protocol, value);
        });
    }

    public Set<String> getProtocols(String endpoint) {
        return adaptors.get(endpoint).keySet();
    }

    @SuppressWarnings("unchecked")
    public <T extends IProtocolAdaptor> T getProtocolAdaptor(String endpoint, String protocol, Class<T> clazz) {
        return  (T) adaptors.get(endpoint).get(protocol);
    }
}
