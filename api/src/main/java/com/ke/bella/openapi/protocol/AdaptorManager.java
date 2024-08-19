package com.ke.bella.openapi.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AdaptorManager {
    private final Map<String, Map<String, IProtocolAdaptor>> adaptors = new ConcurrentHashMap<>();

    public Set<String> getProtocols(String endpoint) {
        return adaptors.get(endpoint).keySet();
    }

    public <T extends IProtocolAdaptor> T getProtocolAdaptor(String endpoint, String protocol, Class<T> clazz) {
        return clazz.cast(adaptors.get(endpoint).get(protocol));
    }

    public synchronized void register(String endpoint, IProtocolAdaptor adaptor) {
        Map<String, IProtocolAdaptor> map = adaptors.computeIfAbsent(endpoint, k -> new HashMap<>());
        map.put(adaptor.getClass().getSimpleName(), adaptor);
    }
}
