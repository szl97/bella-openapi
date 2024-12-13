package com.ke.bella.openapi.configuration;

import com.ke.bella.openapi.db.log.LogRepo;
import com.ke.bella.openapi.protocol.AdaptorManager;
import com.ke.bella.openapi.protocol.IProtocolAdaptor;
import com.ke.bella.openapi.protocol.cost.CostCounter;
import com.ke.bella.openapi.protocol.limiter.LimiterManager;
import com.ke.bella.openapi.protocol.log.CostLogHandler;
import com.ke.bella.openapi.protocol.log.LimiterLogHandler;
import com.ke.bella.openapi.protocol.log.LogEvent;
import com.ke.bella.openapi.protocol.log.LogExceptionHandler;
import com.ke.bella.openapi.protocol.log.LogRecordHandler;
import com.ke.bella.openapi.protocol.log.MetricsLogHandler;
import com.ke.bella.openapi.protocol.metrics.MetricsManager;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.util.List;

@Configuration
public class BellaAutoConf {
    private final SleepingWaitStrategy sleepingWaitStrategy = new SleepingWaitStrategy();
    private Disruptor<LogEvent> logDisruptor;
    @Autowired
    private CostCounter costCounter;
    @Autowired
    private MetricsManager metricsManager;
    @Autowired
    private LimiterManager limiterManager;
    @Bean
    public AdaptorManager adaptorManager(@Autowired List<IProtocolAdaptor> adaptors) {
        AdaptorManager manager = new AdaptorManager();
        adaptors.forEach(adaptor -> manager.register(adaptor.endpoint(), adaptor));
        return manager;
    }

    @Bean
    public RingBuffer<LogEvent> logRingBuffer(List<LogRepo> logRepos) {
        Disruptor<LogEvent> disruptor = new Disruptor<>(LogEvent::new, 1024,
                DaemonThreadFactory.INSTANCE, ProducerType.MULTI, sleepingWaitStrategy);
        disruptor.handleEventsWith(new LogRecordHandler(logRepos), new CostLogHandler(costCounter), new MetricsLogHandler(metricsManager), new LimiterLogHandler(limiterManager));
        disruptor.setDefaultExceptionHandler(new LogExceptionHandler());
        disruptor.start();
        logDisruptor = disruptor;
        return disruptor.getRingBuffer();
    }

    @PreDestroy
    public void shutdownDisruptors() {
        if(logDisruptor != null) {
            logDisruptor.shutdown();
        }
        costCounter.flush();
    }
}
