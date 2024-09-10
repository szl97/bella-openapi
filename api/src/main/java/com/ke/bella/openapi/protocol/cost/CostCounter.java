package com.ke.bella.openapi.protocol.cost;

import com.ke.bella.openapi.service.ApikeyService;
import com.ke.bella.openapi.utils.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class CostCounter {
    @Autowired
    private ApikeyService apikeyService;
    private final ConcurrentHashMap<String, AtomicReference<BigDecimal>> costCache = new ConcurrentHashMap<>();
    public void delta(String apikey, BigDecimal cost) {
        AtomicReference<BigDecimal> amount = costCache.computeIfAbsent(apikey, k -> new AtomicReference<>(BigDecimal.ZERO));
        amount.accumulateAndGet(cost, BigDecimal::add);
    }

    @Scheduled(fixedRate = 60000)
    public synchronized void flush() {
        if(!costCache.isEmpty()) {
            Set<String> current = costCache.keySet();
            String month = DateTimeUtils.getCurrentMonth();
            current.forEach(apikey -> {
                        AtomicReference<BigDecimal> cost = costCache.remove(apikey);
                        apikeyService.recordCost(apikey, month, cost.get());
                    }
            );
        }
    }
}
