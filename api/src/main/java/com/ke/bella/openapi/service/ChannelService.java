package com.ke.bella.openapi.service;

import com.ke.bella.openapi.db.repo.ChannelRepo;
import com.ke.bella.openapi.db.repo.Page;
import com.ke.bella.openapi.dto.Condition;
import com.ke.bella.openapi.dto.MetaDataOps;
import com.ke.bella.openapi.tables.pojos.OpenapiChannelDB;
import com.ke.bella.openapi.tables.pojos.OpenapiEndpointDB;
import com.ke.bella.openapi.tables.pojos.OpenapiModelDB;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;

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

    @Transactional
    public OpenapiChannelDB createChannel(MetaDataOps.ChannelCreateOp op) {
        if(op.getEntityType().equals(ENDPOINT)) {
            OpenapiEndpointDB endpoint = endpointService.getOne(EndpointService.UniqueKeyQuery.builder()
                    .endpoint(op.getEntityCode()).build());
            Assert.notNull(endpoint, "能力点实体不存在");
        } else {
            OpenapiModelDB model = modelService.getOne(op.getEntityCode());
            Assert.notNull(model, "模型实体不存在");
            //todo: 根据模型类型和协议检查channelInfo和priceInfo
        }
        return channelRepo.insert(op);
    }

    @Transactional
    public void updateChannel(MetaDataOps.ChannelUpdateOp op) {
        channelRepo.checkExist(op.getChannelCode(), true);
        if(!StringUtils.isEmpty(op.getPriceInfo()) || !StringUtils.isEmpty(op.getChannelInfo())) {
            Entity entity = getEntityInfoByCode(op.getChannelCode());
            if(entity.getEntityCode().equals(MODEL)) {
                //todo: 修改根据模型类型和协议检查channelInfo,priceInfo
            }
        }
        channelRepo.update(op, op.getChannelCode());
    }

    @Transactional
    public void changeStatus(String channelCode, boolean active) {
        channelRepo.checkExist(channelCode, true);
        if(!active) {
            Entity entity = getEntityInfoByCode(channelCode);
            if(entity.getEntityCode().equals(MODEL)) {
                OpenapiModelDB model = modelService.getActiveByModelName(entity.getEntityCode());
                if(model != null && model.getVisibility().equals(PUBLIC)) {
                    List<OpenapiChannelDB> actives = listActives(MODEL, entity.getEntityCode());
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

    public OpenapiChannelDB getOne(String channelCode) {
        return channelRepo.queryByUniqueKey(channelCode);
    }

    public OpenapiChannelDB getActiveByChannelCode(String channelCode) {
        OpenapiChannelDB db = getOne(channelCode);
        return db == null || db.getStatus().equals(INACTIVE) ? null : db;
    }

    public List<OpenapiChannelDB> listActives(String entityType, String entityCode) {
        return listByCondition(Condition.ChannelCondition.builder()
                .status(ACTIVE)
                .entityType(entityType)
                .entityCode(entityCode)
                .build());
    }

    public List<OpenapiChannelDB> listByCondition(Condition.ChannelCondition condition) {
        return channelRepo.list(condition);
    }

    public Page<OpenapiChannelDB> pageByCondition(Condition.ChannelCondition condition) {
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
