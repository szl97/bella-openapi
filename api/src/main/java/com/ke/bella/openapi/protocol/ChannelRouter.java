package com.ke.bella.openapi.protocol;

import com.ke.bella.openapi.db.TableConstants;
import com.ke.bella.openapi.service.ChannelService;
import com.ke.bella.openapi.tables.pojos.ChannelDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ChannelRouter {
    private final Random random = new Random();
    @Autowired
    private ChannelService channelService;

    public ChannelDB route(String endpoint, String model, Set<String> protocols) {
        List<ChannelDB> channels = channelService.listActives(model == null ?  TableConstants.ENDPOINT : TableConstants.MODEL,
                model == null ? endpoint : model);
        return pick(channels, protocols);
    }

    private ChannelDB pick(List<ChannelDB> channels, Set<String> protocols) {
        Assert.notNull(channels, "没有可用渠道");
        List<ChannelDB> available = availableFilter(channels, protocols);
        Assert.notNull(available, "没有可用渠道");
        List<ChannelDB> highest = pickMaxPriority(channels);
        return route(highest);
    }

    /**
     * 可用： 1、未限流 2、协议可用 3、账户支持的数据流向（风控）
     *
     * @param channels
     * @param protocols
     *
     * @return
     */
    private List<ChannelDB> availableFilter(List<ChannelDB> channels, Set<String> protocols) {
        //todo: 筛选已触发限流的渠道 筛选数据流向符合的
//        Set<String> dataPermission = Arrays.stream(BellaContext.get(RequestInfoContext.Attribute.DATA_PERMISSION)
//                .split(",")).collect(Collectors.toSet());
        return channels.stream().filter(x -> protocols.contains(x.getProtocol())).collect(Collectors.toList());
    }

    private List<ChannelDB> pickMaxPriority(List<ChannelDB> channels) {
        List<ChannelDB> highest = new ArrayList<>();
        String max = TableConstants.LOW;
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
        if(priority.equals(TableConstants.LOW)) {
            return -1;
        }
        if(priority.equals(TableConstants.NORMAL)) {
            if(target.equals(TableConstants.HIGH)) {
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
