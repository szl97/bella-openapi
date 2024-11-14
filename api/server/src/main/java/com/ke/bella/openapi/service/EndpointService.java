package com.ke.bella.openapi.service;

import com.alicp.jetcache.anno.Cached;
import com.google.common.collect.Lists;
import com.ke.bella.openapi.EnumDto;
import com.ke.bella.openapi.db.repo.EndpointRepo;
import com.ke.bella.openapi.db.repo.Page;
import com.ke.bella.openapi.metadata.Condition;
import com.ke.bella.openapi.metadata.EndpointDetails;
import com.ke.bella.openapi.metadata.MetaDataOps;
import com.ke.bella.openapi.metadata.MetadataFeatures;
import com.ke.bella.openapi.metadata.Model;
import com.ke.bella.openapi.metadata.PriceDetails;
import com.ke.bella.openapi.protocol.IPriceInfo;
import com.ke.bella.openapi.tables.pojos.EndpointDB;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ke.bella.openapi.EntityConstants.ACTIVE;
import static com.ke.bella.openapi.EntityConstants.INACTIVE;
import static com.ke.bella.openapi.EntityConstants.INNER;
import static com.ke.bella.openapi.EntityConstants.MAINLAND;
import static com.ke.bella.openapi.EntityConstants.OVERSEAS;

/**
 * Author: Stan Sai Date: 2024/8/2 10:41 description:
 */
@Component
public class EndpointService {
    @Autowired
    private EndpointRepo endpointRepo;
    @Autowired
    private ModelService modelService;
    @Autowired
    private ChannelService channelService;

    @Transactional
    public EndpointDB createEndpoint(MetaDataOps.EndpointOp op) {
        endpointRepo.checkExist(op.getEndpoint(), false);
        return endpointRepo.insert(op);
    }

    @Transactional
    public void updateEndpoint(MetaDataOps.EndpointOp op) {
        endpointRepo.checkExist(op.getEndpoint(), true);
        endpointRepo.update(op, op.getEndpoint());
    }

    @Transactional
    public void changeStatus(String endpoint, boolean active) {
        endpointRepo.checkExist(endpoint, true);
        String status = active ? ACTIVE : INACTIVE;
        endpointRepo.updateStatus(endpoint, status);
    }

    public EndpointDB getActiveByEndpoint(String endpoint) {
        return getActive(UniqueKeyQuery.builder()
                .endpoint(endpoint)
                .build());
    }

    public EndpointDB getOne(UniqueKeyQuery query) {
        if(StringUtils.isNotEmpty(query.getEndpoint())) {
            return endpointRepo.queryByUniqueKey(query.getEndpoint());
        }
        if(StringUtils.isNotEmpty(query.getEndpointCode())) {
            return endpointRepo.queryByEndpointCode(query.getEndpointCode());
        }
        return null;
    }

    public EndpointDB getActive(UniqueKeyQuery query) {
        EndpointDB db = getOne(query);
        return db == null || db.getStatus().equals(INACTIVE) ? null : db;
    }

    @Cached(name = "endpoint:details:", key = "#condition.endpoint + ':' + #identity",
            condition = "(#condition.modelName == null || #condition.modelName == '') "
                    + "&& (#condition.features == null || #condition.features.isEmpty())")
    public EndpointDetails getEndpointDetails(Condition.EndpointDetailsCondition condition, String identity) {
        List<EnumDto> features = MetadataFeatures.listFeatures(condition.getEndpoint());
        EndpointDetails endpoint = EndpointDetails.builder()
                .endpoint(condition.getEndpoint())
                .features(features)
                .build();
        List<Model> models = searchModels(condition);
        endpoint.setModels(models);
        Class<? extends IPriceInfo> priceType = IPriceInfo.EndpointPriceInfoType.fetchType(endpoint.getEndpoint());
        if(priceType != null) {
            Map<String, PriceDetails> priceDetails;
            if(CollectionUtils.isNotEmpty(models)) {
                priceDetails = channelService.getPriceInfo(models.stream().map(Model::getModelName).collect(Collectors.toList()), priceType);
                endpoint.getModels().forEach(model -> {
                    model.setPriceDetails(priceDetails.get(model.getModelName()));
                });
            } else {
                priceDetails = channelService.getPriceInfo(Lists.newArrayList(endpoint.getEndpoint()), priceType);
                endpoint.setPriceDetails(priceDetails.get(endpoint.getEndpoint()));
            }
        }
        return endpoint;
    }

    private List<Model> searchModels(Condition.EndpointDetailsCondition condition) {
        Condition.ModelCondition modelCondition = new Condition.ModelCondition();
        modelCondition.setEndpoint(condition.getEndpoint());
        modelCondition.setModelName(condition.getModelName());
        modelCondition.setStatus(ACTIVE);
        if(CollectionUtils.isNotEmpty(condition.getFeatures())) {
            modelCondition.setFeatures(new ArrayList<>());
            if(!MetadataFeatures.validate(condition.getFeatures())) {
                return Lists.newArrayList();
            }
            condition.getFeatures().forEach(f -> {
                if(f.equals(MetadataFeatures.OVERSEAS.getCode())) {
                    modelCondition.setDataDestination(OVERSEAS);
                } else if(f.equals(MetadataFeatures.MAINLAND.getCode())) {
                    modelCondition.setDataDestination(MAINLAND);
                } else if(f.equals(MetadataFeatures.INNER.getCode())) {
                    modelCondition.setDataDestination(INNER);
                } else if(f.equals(MetadataFeatures.LARGE_INPUT_CONTEXT.getCode())) {
                    modelCondition.setMaxInputTokensLimit(100000);
                } else if(f.equals(MetadataFeatures.LARGE_OUTPUT_CONTEXT.getCode())) {
                    modelCondition.setMaxOutputTokensLimit(8000);
                } else {
                    modelCondition.getFeatures().add(f);
                }
            });
        }
        return modelService.listByConditionWithPermission(modelCondition)
                .stream().map(db -> {
                    Model model = new Model();
                    model.setModelName(db.getModelName());
                    model.setDocumentUrl(db.getDocumentUrl());
                    model.setFeatures(db.getFeatures());
                    model.setProperties(db.getProperties());
                    return model;
                }).sorted(Comparator.comparing(Model::getModelName).reversed())
                .collect(Collectors.toList());
    }

    public List<EndpointDB> listByCondition(Condition.EndpointCondition condition) {
        return endpointRepo.list(condition);
    }

    public <T> List<T> listByCondition(Condition.EndpointCondition condition, Class<T> type) {
        return endpointRepo.list(condition, type);
    }

    public Page<EndpointDB> pageByCondition(Condition.EndpointCondition condition) {
        return endpointRepo.page(condition);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UniqueKeyQuery {
        private String endpoint;
        private String endpointCode;
    }

}
