package com.ke.bella.openapi.protocol;

import com.ke.bella.openapi.EndpointContext;
import com.ke.bella.openapi.common.EntityConstants;
import com.ke.bella.openapi.common.exception.ChannelException;
import com.ke.bella.openapi.protocol.limiter.LimiterManager;
import com.ke.bella.openapi.protocol.metrics.MetricsManager;
import com.ke.bella.openapi.service.ChannelService;
import com.ke.bella.openapi.service.ModelService;
import com.ke.bella.openapi.tables.pojos.ChannelDB;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ke.bella.openapi.common.EntityConstants.LOWEST_SAFETY_LEVEL;

@Component
public class ChannelRouter {
    private final Random random = new Random();
    @Autowired
    private ChannelService channelService;
    @Autowired
    private ModelService modelService;
    @Autowired
    private MetricsManager metricsManager;
    @Autowired
    private LimiterManager limiterManager;
    @Value("${bella.openapi.free.rpm:5}")
    private Integer freeRpm;
    @Value("${bella.openapi.free.concurrent:1}")
    private Integer freeConcurrent;

    public ChannelDB route(String endpoint, String model, boolean isMock) {
        if(isMock) {
            return mockChannel();
        }
        List<ChannelDB> channels;
        String entityCode;
        if(model != null) {
            String terminal = modelService.fetchTerminalModelName(model);
            entityCode = terminal;
            channels = channelService.listActives(EntityConstants.MODEL, terminal);
        } else {
            entityCode = endpoint;
            channels = channelService.listActives(EntityConstants.ENDPOINT, endpoint);
        }
        Assert.isTrue(CollectionUtils.isNotEmpty(channels), "没有可用渠道");
        channels = filter(channels, entityCode);
        channels = pickMaxPriority(channels);
        return random(channels);
    }

    /**
     * 1、筛选账户支持的数据流向（风控） 2、筛选可用的渠道
     *
     * @param channels
     *
     * @return
     */
    private List<ChannelDB> filter(List<ChannelDB> channels, String entityCode) {
        Byte safetyLevel = EndpointContext.getApikey().getSafetyLevel();
        List<ChannelDB> filtered = channels.stream().filter(channel -> getSafetyLevelLimit(channel.getDataDestination()) <= safetyLevel)
                .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(filtered)) {
            if(LOWEST_SAFETY_LEVEL.equals(safetyLevel)) {
                filtered = channels.stream().filter(this::isTestUsed)
                        .collect(Collectors.toList());
            }
            if(CollectionUtils.isEmpty(filtered)) {
                throw new ChannelException.AuthorizationException("未经安全合规审核，没有使用权限");
            }
            if(freeAkOverload(EndpointContext.getProcessData().getAkCode(), entityCode)) {
                throw new ChannelException.RateLimitException("当前使用试用额度,每分钟最多请求" + freeRpm + "次, 且不能高于" + freeConcurrent);
            }
        }
        Set<String> unavailableSet = metricsManager.getAllUnavailableChannels(
                filtered.stream().map(ChannelDB::getChannelCode).collect(Collectors.toList()));
        filtered = filtered.stream()
                .filter(channel -> channel.getDataDestination().equals(EntityConstants.PROTECTED) ||
                        channel.getDataDestination().equals(EntityConstants.INNER) ||
                        !unavailableSet.contains(channel.getChannelCode()))
                .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(filtered)) {
            throw new ChannelException.RateLimitException("渠道当前负载过高，请稍后重试");
        }
        return filtered;
    }

    private boolean isTestUsed(ChannelDB channel) {
        return 1 == channel.getTrialEnabled();
    }

    private boolean freeAkOverload(String akCode, String entityCode) {
        return limiterManager.getRequestCountPerMinute(akCode, entityCode) >= freeRpm
                || limiterManager.getCurrentConcurrentCount(akCode, entityCode) >= freeConcurrent;
    }

    private Byte getSafetyLevelLimit(String dataDestination) {
        switch (dataDestination) {
        case EntityConstants.PROTECTED:
            return 10;
        case EntityConstants.INNER:
            return 20;
        case EntityConstants.MAINLAND:
            return 30;
        case EntityConstants.OVERSEAS:
            return 40;
        }
        return 40;
    }

    private List<ChannelDB> pickMaxPriority(List<ChannelDB> channels) {
        List<ChannelDB> highest = new ArrayList<>();
        String max = EntityConstants.LOW;
        for (ChannelDB channel : channels) {
            String priority = channel.getPriority();
            int compare = compare(priority, max);
            if(compare < 0) {
                continue;
            }
            if(compare > 0) {
                highest.clear();
                max = priority;
            }
            highest.add(channel);
        }
        return highest;
    }

    private int compare(String priority, String target) {
        if(priority.equals(target)) {
            return 0;
        }
        if(priority.equals(EntityConstants.LOW)) {
            return -1;
        }
        if(priority.equals(EntityConstants.NORMAL)) {
            if(target.equals(EntityConstants.HIGH)) {
                return -1;
            }
        }
        return 1;
    }

    private ChannelDB random(List<ChannelDB> list) {
        if(list.size() == 1) {
            return list.get(0);
        }
        int rand = random.nextInt(list.size());
        return list.get(rand);
    }

    private ChannelDB mockChannel() {
        ChannelDB channel = new ChannelDB();
        channel.setChannelCode("ch-mock");
        channel.setProtocol("MockAdaptor");
        channel.setEntityType(EntityConstants.ENDPOINT);
        channel.setEntityCode("mock");
        channel.setPriceInfo("{}");
        channel.setChannelInfo("{}");
        channel.setSupplier("AIT");
        channel.setUrl("");
        return channel;
    }

}
