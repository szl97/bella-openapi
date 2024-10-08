package com.ke.bella.openapi.protocol;

import com.ke.bella.openapi.EntityConstants;
import com.ke.bella.openapi.service.ChannelService;
import com.ke.bella.openapi.service.ModelService;
import com.ke.bella.openapi.tables.pojos.ChannelDB;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class ChannelRouter {
    private final Random random = new Random();
    @Autowired
    private ChannelService channelService;
    @Autowired
    private ModelService modelService;

    public ChannelDB route(String endpoint, String model) {
        List<ChannelDB> channels;
        if(model != null) {
            String terminal = modelService.fetchTerminalModelName(model);
            channels = channelService.listActives(EntityConstants.MODEL, terminal);
        } else {
            channels = channelService.listActives(EntityConstants.MODEL,endpoint);
        }
        return pick(channels);
    }

    private ChannelDB pick(List<ChannelDB> channels) {
        Assert.isTrue(CollectionUtils.isNotEmpty(channels), "没有可用渠道");
        List<ChannelDB> available = availableFilter(channels);
        Assert.isTrue(CollectionUtils.isNotEmpty(available), "没有可用渠道");
        List<ChannelDB> highest = pickMaxPriority(channels);
        return route(highest);
    }

    /**
     * 可用： 1、未限流 2、协议可用 3、账户支持的数据流向（风控）
     *
     * @param channels
     *
     * @return
     */
    private List<ChannelDB> availableFilter(List<ChannelDB> channels) {
        //todo: 筛选已触发限流的渠道 筛选数据流向符合的
//        Set<String> dataPermission = Arrays.stream(BellaContext.get(RequestInfoContext.Attribute.DATA_PERMISSION)
//                .split(",")).collect(Collectors.toSet());
        return channels;
    }

    private List<ChannelDB> pickMaxPriority(List<ChannelDB> channels) {
        List<ChannelDB> highest = new ArrayList<>();
        String max = EntityConstants.LOW;
        for (ChannelDB channel : channels) {
            int compare = compare(channel.getPriority(), max);
            if(compare < 0) {
                continue;
            }
            if(compare > 0) {
                highest.clear();
                max = channel.getPriority();
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

    private ChannelDB route(List<ChannelDB> channels) {
        if(channels.size() == 1) {
            return channels.get(0);
        }
        List<ChannelDB> minUsages = pickMinUsageChannels(channels);
        return minUsages.get(random.nextInt(minUsages.size()));
    }

    private List<ChannelDB> pickMinUsageChannels(List<ChannelDB> channels) {
        //todo: 选择资源利用率最小的 (没有资源利用率记录的默认会被选中)
        return channels;
    }

}
