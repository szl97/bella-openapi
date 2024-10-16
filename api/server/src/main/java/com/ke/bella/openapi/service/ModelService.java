package com.ke.bella.openapi.service;

import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.template.QuickConfig;
import com.google.common.collect.Sets;
import com.ke.bella.openapi.EntityConstants;
import com.ke.bella.openapi.Operator;
import com.ke.bella.openapi.console.ConsoleContext;
import com.ke.bella.openapi.db.repo.ModelRepo;
import com.ke.bella.openapi.db.repo.Page;
import com.ke.bella.openapi.metadata.Condition;
import com.ke.bella.openapi.metadata.MetaDataOps;
import com.ke.bella.openapi.tables.pojos.EndpointDB;
import com.ke.bella.openapi.tables.pojos.ModelAuthorizerRelDB;
import com.ke.bella.openapi.tables.pojos.ModelDB;
import com.ke.bella.openapi.tables.pojos.ModelEndpointRelDB;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.ke.bella.openapi.EntityConstants.ACTIVE;
import static com.ke.bella.openapi.EntityConstants.INACTIVE;
import static com.ke.bella.openapi.EntityConstants.PRIVATE;
import static com.ke.bella.openapi.EntityConstants.PUBLIC;
import static com.ke.bella.openapi.console.MetadataValidator.generateInvalidModelJsonKeyMessage;
import static com.ke.bella.openapi.console.MetadataValidator.json2Map;
import static com.ke.bella.openapi.console.MetadataValidator.matchPath;

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
    private ApikeyService apikeyService;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private ApplicationContext applicationContext;
    private static final String modelTerminalCacheKey = "model:terminal:";
    private static final String modelMapCacheKey = "model:map:";


    @PostConstruct
    public void postConstruct() {
        QuickConfig modelTerminalConfig = QuickConfig.newBuilder(modelTerminalCacheKey)
                .localLimit(500)
                .cacheNullValue(true)
                .cacheType(CacheType.BOTH)
                .syncLocal(true)
                .penetrationProtect(true)
                .penetrationProtectTimeout(Duration.ofSeconds(10))
                .build();
        cacheManager.getOrCreateCache(modelTerminalConfig);
        QuickConfig modelCacheConfig = QuickConfig.newBuilder(modelMapCacheKey)
                .expire(Duration.ofDays(3650))
                .localExpire(Duration.ofMinutes(5))
                .localLimit(1)
                .cacheNullValue(true)
                .cacheType(CacheType.BOTH)
                .syncLocal(true)
                .penetrationProtect(true)
                .penetrationProtectTimeout(Duration.ofSeconds(10))
                .build();
        cacheManager.getOrCreateCache(modelCacheConfig);
    }

    @Transactional
    public ModelDB createModel(MetaDataOps.ModelOp op) {
        modelRepo.checkExist(op.getModelName(), false);
        checkEndpoint(op.getEndpoints());
        checkPropertyAndFeatures(op.getProperties(), op.getFeatures(), op.getEndpoints(), op.getModelName());
        ModelDB db = modelRepo.insert(op);
        modelRepo.batchInsertModelEndpoints(op.getModelName(), op.getEndpoints());
        updateModelCache(op.getModelName(), op.getModelName());
        return db;
    }

    @Transactional
    public void updateModel(MetaDataOps.ModelOp op) {
        modelRepo.checkExist(op.getModelName(), true);
        if(CollectionUtils.isNotEmpty(op.getEndpoints())) {
            checkEndpoint(op.getEndpoints());
            List<ModelEndpointRelDB> originEndpoints = modelRepo.listEndpointsByModelName(op.getModelName());
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
            if(CollectionUtils.isNotEmpty(deletes)) {
                modelRepo.batchDeleteModelEndpoints(deletes);
            }
            if(CollectionUtils.isNotEmpty(inserts)) {
                modelRepo.batchInsertModelEndpoints(op.getModelName(), inserts);
            }
        }
        checkPropertyAndFeatures(op.getProperties(), op.getFeatures(), op.getEndpoints(), op.getModelName());
        if(StringUtils.isNotEmpty(op.getDocumentUrl()) || StringUtils.isNotEmpty(op.getProperties())
                || StringUtils.isNotEmpty(op.getFeatures())) {
            modelRepo.update(op, op.getModelName());
        }
        updateModelCache(op.getModelName(), null);
    }

    private void checkPropertyAndFeatures(String properties, String features, Set<String> endpoints, String model) {
        if(StringUtils.isEmpty(properties) && StringUtils.isEmpty(features)) {
            return;
        }
        List<String> basisEndpoints = getAllBasicEndpoints(endpoints, model);
        if(StringUtils.isNotEmpty(properties)) {
            checkJson(properties, "properties", basisEndpoints);
        }
        if(StringUtils.isNotEmpty(features)) {
            checkJson(features, "features", basisEndpoints);
        }
    }

    private void checkJson(String jsonStr, String field, List<String> basisEndpoints) {
        if(jsonStr.equals("{}")) {
            return;
        }
        Map<String, Object> map = json2Map(jsonStr);
        Assert.isTrue(map != null && !map.isEmpty(), field + "非json格式");
        for (String endpoint : basisEndpoints) {
            String invalidMessage = generateInvalidModelJsonKeyMessage(map, endpoint, field);
            Assert.isNull(invalidMessage, invalidMessage);
        }
    }

    private List<String> getAllBasicEndpoints(Set<String> endpoints, String model) {
        List<ModelEndpointRelDB> dbs = modelRepo.listEndpointsByModelName(model);
        Set<String> set = dbs.stream().map(ModelEndpointRelDB::getEndpoint).collect(Collectors.toSet());
        if(CollectionUtils.isNotEmpty(endpoints)) {
            set.addAll(endpoints);
        }
        return set.stream().filter(path ->
                Arrays.stream(EntityConstants.SystemBasicEndpoint.values())
                        .map(EntityConstants.SystemBasicEndpoint::getEndpoint)
                        .anyMatch(match -> matchPath(match, path))).collect(Collectors.toList());
    }

    public List<String> getAllEndpoints(String model) {
        List<ModelEndpointRelDB> dbs = modelRepo.listEndpointsByModelName(model);
        return dbs.stream().map(ModelEndpointRelDB::getEndpoint).collect(Collectors.toList());
    }

    private void checkEndpoint(Set<String> endpoints) {
        Condition.EndpointCondition condition = Condition.EndpointCondition.builder()
                .endpoints(endpoints)
                .build();
        List<EndpointDB> dbs = endpointService.listByCondition(condition);
        Assert.isTrue(dbs.size() == endpoints.size(), () -> {
            Set<String> temp = Sets.newHashSet(endpoints);
            dbs.stream().map(EndpointDB::getEndpoint).collect(Collectors.toList()).forEach(temp::remove);
            return "能力点不存在：\r\n" + String.join(",", temp);
        });
    }

    @Transactional
    public void changeStatus(String modelName, boolean active) {
        modelRepo.checkExist(modelName, true);
        String status = active ? ACTIVE : INACTIVE;
        modelRepo.updateStatus(modelName, status);
        updateModelCache(modelName, null);
    }

    @Transactional
    public void changeVisibility(String modelName, boolean publish) {
        modelRepo.checkExist(modelName, true);
        String visibility = publish ? PUBLIC : PRIVATE;
        modelRepo.updateVisibility(modelName, visibility);
        updateModelCache(modelName, null);
    }

    @Transactional
    public void modelAuthorize(MetaDataOps.ModelAuthorizerOp op) {
        modelRepo.checkExist(op.getModel(), true);
        checkOwnerPermission(op.getModel());
        List<ModelAuthorizerRelDB> origins = modelRepo.listAuthorizersByModelName(op.getModel());
        Set<MetaDataOps.ModelAuthorizer> inserts = Sets.newHashSet(op.getAuthorizers());
        List<Long> deletes = new ArrayList<>();
        origins.forEach(origin -> {
                    MetaDataOps.ModelAuthorizer authorizer = MetaDataOps.ModelAuthorizer
                            .builder()
                            .authorizerType(origin.getAuthorizerType())
                            .authorizerCode(origin.getAuthorizerCode())
                            .build();
                    if(op.getAuthorizers().contains(authorizer)) {
                        inserts.remove(authorizer);
                    } else {
                        deletes.add(origin.getId());
                    }
                }
        );
        if(CollectionUtils.isNotEmpty(deletes)) {
            modelRepo.batchDeleteModelAuthorizers(deletes);
        }
        if(CollectionUtils.isNotEmpty(inserts)) {
            modelRepo.batchInsertModelAuthorizers(op.getModel(), inserts);
        }
    }

    @Transactional
    public void modelLink(MetaDataOps.ModelLinkOp op) {
        cacheManager.getCache(modelMapCacheKey).tryLockAndRun("lock", 10, TimeUnit.SECONDS, ()-> {
            doModelLink(op);
        });
    }

    private void doModelLink(MetaDataOps.ModelLinkOp op) {
        ModelDB db = modelRepo.queryByUniqueKeyForUpdateNoWait(op.getModelName());
        Assert.isTrue(db != null, "实体不存在");
        if(db.getLinkedTo().equals(op.getLinkedTo())) {
            return;
        }
        List<String> path = getPath(op.getLinkedTo());
        Assert.isTrue(CollectionUtils.isNotEmpty(path), "linedTo实体不存在");
        Assert.isTrue(!path.contains(op.getModelName()), "出现循环");
        String terminal = path.get(path.size() - 1);
        modelRepo.update(op, op.getModelName());
        updateModelCache(op.getModelName(), terminal);
    }

    @Cached(name = modelTerminalCacheKey, key = "#modelName")
    public String fetchTerminalModelName(String modelName) {
        List<String> path = getPath(modelName);
        return CollectionUtils.isEmpty(path) ? modelName : path.get(path.size() - 1);
    }


    @Cached(name = modelMapCacheKey, key = "#key")
    public Map<String, ModelDB> queryWithCache(String key) {
        List<ModelDB> list = modelRepo.listAll();
        return list.stream().collect(Collectors.toMap(ModelDB::getModelName, Function.identity()));
    }

    public List<String> getPath(String target) {
        Map<String, ModelDB> map = applicationContext.getBean(ModelService.class).queryWithCache("all");
        List<String> path = new ArrayList<>();
        String name = target;
        while(StringUtils.isNotEmpty(name) && map.containsKey(name)) {
            path.add( name);
            name = map.get(name).getLinkedTo();
        }
        return path;
    }

    private void updateModelCache(String modelName, String terminal) {
        cacheManager.getCache(modelMapCacheKey).tryLockAndRun("lock", 10, TimeUnit.SECONDS, ()-> {
            ModelDB modelDB = modelRepo.queryByUniqueKey(modelName);
            Map<String, ModelDB> map = applicationContext.getBean(ModelService.class).queryWithCache("all");
            map.put(modelName, modelDB);
            cacheManager.getCache(modelMapCacheKey).put("all", map);
            if(StringUtils.isNotEmpty(terminal)) {
                cacheManager.getCache(modelTerminalCacheKey).put(modelDB, terminal);
            }
        });
    }

    private void checkOwnerPermission(String model) {
        ModelDB db = modelRepo.queryByUniqueKey(model);
        Operator operator = ConsoleContext.getOperator();
        //todo: 检查 operator 是否是 Owner
    }

    public ModelDB getActiveByModelName(String modelName) {
        ModelDB db = getOne(modelName);
        return db == null || db.getStatus().equals(INACTIVE) ? null : db;
    }

    public ModelDB getOne(String modelName) {
        return modelRepo.queryByUniqueKey(modelName);
    }

    public ModelDB getOneForUpdate(String modelName) {
        return modelRepo.queryByUniqueKeyForUpdate(modelName);
    }

    public List<ModelDB> listByCondition(Condition.ModelCondition condition) {
        return modelRepo.list(condition);
    }

    public Page<ModelDB> pageByCondition(Condition.ModelCondition condition) {
        return modelRepo.page(condition);
    }

    public List<ModelDB> listByConditionWithPermission(Condition.ModelCondition condition) {
        apikeyService.fillPermissionCode(condition);
        return listByCondition(condition);
    }

    public Page<ModelDB> pageByConditionWithPermission(Condition.ModelCondition condition) {
        apikeyService.fillPermissionCode(condition);
        return pageByCondition(condition);
    }
}
