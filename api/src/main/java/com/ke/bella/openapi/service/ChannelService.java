package com.ke.bella.openapi.service;

import com.ke.bella.openapi.console.MetaDataOps;
import com.ke.bella.openapi.db.repo.ChannelRepo;
import com.ke.bella.openapi.db.repo.Page;
import com.ke.bella.openapi.protocol.AdaptorManager;
import com.ke.bella.openapi.protocol.cost.CostCalculator;
import com.ke.bella.openapi.protocol.metadata.Condition;
import com.ke.bella.openapi.tables.pojos.ChannelDB;
import com.ke.bella.openapi.tables.pojos.EndpointDB;
import com.ke.bella.openapi.tables.pojos.ModelDB;
import com.ke.bella.openapi.tables.pojos.ModelEndpointRelDB;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.ke.bella.openapi.db.TableConstants.ACTIVE;
import static com.ke.bella.openapi.db.TableConstants.ENDPOINT;
import static com.ke.bella.openapi.db.TableConstants.INACTIVE;
import static com.ke.bella.openapi.db.TableConstants.MODEL;
import static com.ke.bella.openapi.db.TableConstants.PUBLIC;

/**
 * Author: Stan Sai Date: 2024/8/2 11:35 description:
 */
@Component
public class ChannelService {
    @Autowired
    private ChannelRepo channelRepo;
    @Autowired
    private EndpointService endpointService;
    @Autowired
    private ModelService modelService;
    @Autowired
    private AdaptorManager adaptorManager;

    @Transactional
    public ChannelDB createChannel(MetaDataOps.ChannelCreateOp op) {
        List<String> endpoints = new ArrayList<>();
        if(op.getEntityType().equals(ENDPOINT)) {
            EndpointDB endpoint = endpointService.getOne(EndpointService.UniqueKeyQuery.builder()
                    .endpoint(op.getEntityCode()).build());
            Assert.notNull(endpoint, "能力点实体不存在");
            endpoints.add(endpoint.getEndpoint());
        } else {
            ModelDB model = modelService.getOne(op.getEntityCode());
            Assert.notNull(model, "模型实体不存在");
            endpoints = modelService.getAllEndpoints(model.getModelName());

        }
        endpoints.forEach(endpoint -> Assert.isTrue(CostCalculator.validate(endpoint, op.getPriceInfo()), "priceInfo invalid"));
        endpoints.forEach(endpoint -> Assert.isTrue(adaptorManager.support(endpoint, op.getProtocol()), "不支持的协议"));
        //todo: 根据协议检查channelInfo
        return channelRepo.insert(op);
    }

    @Transactional
    public void updateChannel(MetaDataOps.ChannelUpdateOp op) {
        channelRepo.checkExist(op.getChannelCode(), true);
        if(StringUtils.isNotEmpty(op.getPriceInfo())) {
            List<String> endpoints = new ArrayList<>();
            Entity entity = getEntityInfoByCode(op.getChannelCode());
            if(entity.getEntityCode().equals(MODEL)) {
                ModelDB model = modelService.getOne(entity.getEntityCode());
                endpoints = modelService.getAllEndpoints(model.getModelName());
            } else {
                endpoints.add(entity.getEntityCode());
            }
            endpoints.forEach(endpoint -> Assert.isTrue(CostCalculator.validate(endpoint, op.getPriceInfo()), "priceInfo invalid"));
        }
        //todo: 根据协议检查channelInfo
        channelRepo.update(op, op.getChannelCode());
    }

    @Transactional
    public void changeStatus(String channelCode, boolean active) {
        channelRepo.checkExist(channelCode, true);
        if(!active) {
            Entity entity = getEntityInfoByCode(channelCode);
            if(entity.getEntityCode().equals(MODEL)) {
                ModelDB model = modelService.getActiveByModelName(entity.getEntityCode());
                if(model != null && model.getVisibility().equals(PUBLIC)) {
                    List<ChannelDB> actives = listActives(MODEL, entity.getEntityCode());
                    boolean moreThanOneActive = actives.stream()
                            .anyMatch(channel -> !channel.getChannelCode().equals(channelCode));
                    Assert.isTrue(moreThanOneActive, "已发布模型至少要有一个可用渠道");
                }
            }
        }
        String status = active ? ACTIVE : INACTIVE;
        channelRepo.updateStatus(channelCode, status);
    }

    public Entity getEntityInfoByCode(String code) {
        String[] splits = code.split("_");
        return Entity.builder()
                .entityType(splits[0])
                .entityCode(splits[1])
                .build();
    }

    public ChannelDB getOne(String channelCode) {
        return channelRepo.queryByUniqueKey(channelCode);
    }

    public ChannelDB getActiveByChannelCode(String channelCode) {
        ChannelDB db = getOne(channelCode);
        return db == null || db.getStatus().equals(INACTIVE) ? null : db;
    }

    public List<ChannelDB> listActives(String entityType, String entityCode) {
        return listByCondition(Condition.ChannelCondition.builder()
                .status(ACTIVE)
                .entityType(entityType)
                .entityCode(entityCode)
                .build());
    }

    public List<ChannelDB> listByCondition(Condition.ChannelCondition condition) {
        return channelRepo.list(condition);
    }

    public Page<ChannelDB> pageByCondition(Condition.ChannelCondition condition) {
        return channelRepo.page(condition);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Entity {
        private String entityType;
        private String entityCode;
    }
}
