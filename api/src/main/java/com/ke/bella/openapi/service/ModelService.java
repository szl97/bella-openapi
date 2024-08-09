package com.ke.bella.openapi.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ke.bella.openapi.db.repo.ModelRepo;
import com.ke.bella.openapi.db.repo.Page;
import com.ke.bella.openapi.dto.Condition;
import com.ke.bella.openapi.dto.MetaDataOps;
import com.ke.bella.openapi.tables.pojos.OpenapiChannelDB;
import com.ke.bella.openapi.tables.pojos.OpenapiEndpointDB;
import com.ke.bella.openapi.tables.pojos.OpenapiModelDB;
import com.ke.bella.openapi.tables.pojos.OpenapiModelEndpointRelationDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ke.bella.openapi.db.TableConstants.ACTIVE;
import static com.ke.bella.openapi.db.TableConstants.INACTIVE;
import static com.ke.bella.openapi.db.TableConstants.MODEL;
import static com.ke.bella.openapi.db.TableConstants.PRIVATE;
import static com.ke.bella.openapi.db.TableConstants.PUBLIC;

/**
 * Author: Stan Sai Date: 2024/8/2 11:31 description:
 */
@Component
public class ModelService {
    @Autowired
    private ModelRepo modelRepo;
    @Autowired
    private EndpointService endpointService;
    @Autowired
    private ChannelService channelService;

    @Transactional
    public OpenapiModelDB createModel(MetaDataOps.ModelOp op) {
        modelRepo.checkExist(op.getModelName(), false);
        checkEndpoint(op.getEndpoints());
        OpenapiModelDB db = modelRepo.insert(op);
        modelRepo.batchInsertModelEndpoints(op.getModelName(), op.getEndpoints());
        return db;
    }

    @Transactional
    public void updateModel(MetaDataOps.ModelOp op) {
        modelRepo.checkExist(op.getModelName(), true);
        if(!CollectionUtils.isEmpty(op.getEndpoints())) {
            checkEndpoint(op.getEndpoints());
            List<OpenapiModelEndpointRelationDB> originEndpoints = modelRepo.listEndpointsByModelName(op.getModelName());
            Set<String> inserts = Sets.newHashSet(op.getEndpoints());
            List<Long> deletes = new ArrayList<>();
            originEndpoints.forEach(origin -> {
                        if(op.getEndpoints().contains(origin.getEndpoint())) {
                            inserts.remove(origin.getEndpoint());
                        } else {
                            deletes.add(origin.getId());
                        }
                    }
            );
            if(!CollectionUtils.isEmpty(deletes)) {
                modelRepo.batchDeleteModelEndpoints(deletes);
            }
            if(!CollectionUtils.isEmpty(inserts)) {
                modelRepo.batchInsertModelEndpoints(op.getModelName(), inserts);
            }
        }
        if(!StringUtils.isEmpty(op.getDocumentUrl()) || !StringUtils.isEmpty(op.getProperties())
                || !StringUtils.isEmpty(op.getFeatures())) {
            modelRepo.update(op, op.getModelName());
        }
    }

    private void checkEndpoint(Set<String> endpoints) {
        Condition.EndpointCondition condition = Condition.EndpointCondition.builder()
                .endpoints(endpoints)
                .build();
        List<OpenapiEndpointDB> dbs = endpointService.listByCondition(condition);
        Assert.isTrue(dbs.size() == endpoints.size(), () -> {
            Set<String> temp = Sets.newHashSet(endpoints);
            dbs.stream().map(OpenapiEndpointDB::getEndpoint).collect(Collectors.toList()).forEach(temp::remove);
            return "能力点不存在：\r\n" + String.join(",", temp);
        });
    }

    @Transactional
    public void changeStatus(String modelName, boolean active) {
        modelRepo.checkExist(modelName, true);
        String status = active ? ACTIVE : INACTIVE;
        modelRepo.updateStatus(modelName, status);
    }

    @Transactional
    public void changeVisibility(String modelName, boolean publish) {
        modelRepo.checkExist(modelName, true);
        String visibility = publish ? PUBLIC : PRIVATE;
        if(publish) {
            List<OpenapiChannelDB> actives = channelService.listActives(MODEL, modelName);
            Assert.notEmpty(actives, "模型至少有一个可用渠道才可以发布");
        }
        modelRepo.updateVisibility(modelName, visibility);
    }

    public OpenapiModelDB getActiveByModelName(String modelName) {
        OpenapiModelDB db = getOne(modelName);
        return db == null || db.getStatus().equals(INACTIVE) ? null : db;
    }

    public OpenapiModelDB getOne(String modelName) {
        return modelRepo.queryByUniqueKey(modelName);
    }

    public List<OpenapiModelDB> listByCondition(Condition.ModelCondition condition) {
        if(!fillModelNames(condition)) {
            return Lists.newArrayList();
        }
        return modelRepo.list(condition);
    }

    public Page<OpenapiModelDB> pageByCondition(Condition.ModelCondition condition) {
        if(!fillModelNames(condition)) {
            return Page.from(condition.getPageNum(), condition.getPageSize());
        }
        return modelRepo.page(condition);
    }

    private boolean fillModelNames(Condition.ModelCondition condition) {
        if(!StringUtils.isEmpty(condition.getEndpoint())) {
            List<String> modelNames = modelRepo.listModelNamesByEndpoint(condition.getEndpoint());
            if(CollectionUtils.isEmpty(modelNames)) {
                return false;
            }
            if(condition.getModelNames() == null) {
                condition.setModelNames(new HashSet<>());
            }
            condition.getModelNames().addAll(modelNames);
        }
        return true;
    }

}
