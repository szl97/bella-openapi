package com.ke.bella.openapi.protocol.log;

import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.protocol.cost.CostCalculator;
import com.ke.bella.openapi.protocol.cost.CostCounter;
import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

@Slf4j
public class CostLogHandler implements EventHandler<LogEvent> {
    public CostLogHandler(CostCounter costCounter) {
        this.costCounter = costCounter;
    }

    private final CostCounter costCounter;
    @Override
    public void onEvent(LogEvent event, long sequence, boolean endOfBatch) throws Exception {
        EndpointProcessData log = event.getData();
        if(log.isPrivate()) {
            return;
        }
        if(log.getPriceInfo() == null) {
            LOGGER.warn("price Info is null, channelCode:{}, requestId:{}", log.getChannelCode(), log.getRequestId());
            return;
        }
        if(log.getUsage() == null) {
            LOGGER.warn("usage is null, endpoint:{}, requestId:{}", log.getEndpoint(), log.getRequestId());
            return;
        }
        BigDecimal cost = CostCalculator.calculate(log.getEndpoint(), log.getPriceInfo(), log.getUsage());
        if(cost != null && BigDecimal.ZERO.compareTo(cost) < 0) {
            costCounter.delta(log.getAkCode(), cost);
            if(StringUtils.isNotEmpty(log.getParentAkCode())) {
                costCounter.delta(log.getParentAkCode(), cost);
            }
        }
    }
}
