package com.ke.bella.openapi.db.repo;

import com.ke.bella.openapi.metadata.Condition;
import com.ke.bella.openapi.metadata.MetaDataOps;
import com.ke.bella.openapi.tables.pojos.ModelAuthorizerRelDB;
import com.ke.bella.openapi.tables.pojos.ModelDB;
import com.ke.bella.openapi.tables.pojos.ModelEndpointRelDB;
import com.ke.bella.openapi.tables.records.ModelAuthorizerRelRecord;
import com.ke.bella.openapi.tables.records.ModelEndpointRelRecord;
import com.ke.bella.openapi.tables.records.ModelRecord;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.SelectSeekStep1;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.ke.bella.openapi.Tables.MODEL;
import static com.ke.bella.openapi.Tables.MODEL_AUTHORIZER_REL;
import static com.ke.bella.openapi.Tables.MODEL_ENDPOINT_REL;
import static com.ke.bella.openapi.EntityConstants.ORG;
import static com.ke.bella.openapi.EntityConstants.PERSON;
import static com.ke.bella.openapi.EntityConstants.PUBLIC;

/**
 * Author: Stan Sai Date: 2024/8/1 20:34 description:
 */
@Component
public class ModelRepo extends StatusRepo<ModelDB, ModelRecord, String> {

    @Transactional
    public void updateVisibility(String modelName, String visibility) {
        ModelRecord rec = MODEL.newRecord();
        rec.setVisibility(visibility);
        fillUpdatorInfo(rec);
        int num = db.update(MODEL)
                .set(rec)
                .where(MODEL.MODEL_NAME.eq(modelName))
                .execute();
        Assert.isTrue(num == 1, "模型实体更新失败，请检查模型实体是否存在");
    }

    public List<ModelDB> list(Condition.ModelCondition op) {
        return constructSql(op).fetchInto(ModelDB.class);
    }

    public Page<ModelDB> page(Condition.ModelCondition op) {
        return queryPage(db, constructSql(op), op.getPage(), op.getSize(), ModelDB.class);
    }

    private SelectSeekStep1<Record, Long> constructSql(Condition.ModelCondition op) {
        SelectJoinStep step = db.select(MODEL.fields())
                .from(MODEL);
        if(StringUtils.isNotEmpty(op.getEndpoint()) || CollectionUtils.isNotEmpty(op.getEndpoints())) {
            step = step.leftJoin(MODEL_ENDPOINT_REL)
                    .on(MODEL.MODEL_NAME.eq(MODEL_ENDPOINT_REL.MODEL_NAME));
        }
        if(StringUtils.isNotEmpty(op.getPersonalCode()) || CollectionUtils.isNotEmpty(op.getOrgCodes())) {
            step = step.leftJoin(MODEL_AUTHORIZER_REL)
                    .on(MODEL.MODEL_NAME.eq(MODEL_AUTHORIZER_REL.MODEL_NAME));
        }
        return step.where(StringUtils.isEmpty(op.getModelName()) ? DSL.noCondition() : MODEL.MODEL_NAME.like(op.getModelName() + "%"))
                .and(StringUtils.isEmpty(op.getOwnerName()) ? DSL.noCondition() : MODEL.OWNER_NAME.eq(op.getOwnerName()))
                .and(CollectionUtils.isEmpty(op.getModelNames()) ? DSL.noCondition() : MODEL.MODEL_NAME.in(op.getModelNames()))
                .and(StringUtils.isEmpty(op.getVisibility()) ? DSL.noCondition() : MODEL.VISIBILITY.eq(op.getVisibility()))
                .and(StringUtils.isEmpty(op.getStatus()) ? DSL.noCondition() : MODEL.STATUS.eq(op.getStatus()))
                .and(StringUtils.isEmpty(op.getEndpoint()) ? DSL.noCondition() : MODEL_ENDPOINT_REL.ENDPOINT.eq(op.getEndpoint()))
                .and(CollectionUtils.isEmpty(op.getEndpoints()) ? DSL.noCondition() : MODEL_ENDPOINT_REL.ENDPOINT.in(op.getEndpoints()))
                .and(permissionCondition(op.getPersonalCode(), op.getOrgCodes()))
                .orderBy(MODEL.ID.desc());
    }

    private org.jooq.Condition permissionCondition(String personalCode, Set<String> orgCodes) {
        if(StringUtils.isEmpty(personalCode) && CollectionUtils.isEmpty(orgCodes)) {
            return DSL.noCondition();
        }
        org.jooq.Condition condition = MODEL.VISIBILITY.eq(PUBLIC);
        if(StringUtils.isNotEmpty(personalCode)) {
            condition = condition.or(MODEL_AUTHORIZER_REL.AUTHORIZER_TYPE.eq(PERSON)
                            .and(MODEL_AUTHORIZER_REL.AUTHORIZER_CODE.eq(personalCode)));
        }
        if(CollectionUtils.isNotEmpty(orgCodes)) {
                condition = condition.or(MODEL_AUTHORIZER_REL.AUTHORIZER_TYPE.eq(ORG)
                                .and(MODEL_AUTHORIZER_REL.AUTHORIZER_CODE.in(orgCodes)));

        }
        return condition;
    }

    @Transactional
    public int batchInsertModelEndpoints(String modelName, Collection<String> endpoints) {
        List<ModelEndpointRelRecord> records = new ArrayList<>();
        for (String endpoint : endpoints) {
            ModelEndpointRelRecord rec = MODEL_ENDPOINT_REL.newRecord();
            rec.setModelName(modelName);
            rec.setEndpoint(endpoint);
            fillCreatorInfo(rec);
            records.add(rec);
        }
        return batchInsert(db, records);
    }

    @Transactional
    public int batchDeleteModelEndpoints(List<Long> ids) {
        return db.deleteFrom(MODEL_ENDPOINT_REL)
                .where(MODEL_ENDPOINT_REL.ID.in(ids))
                .execute();
    }

    public List<ModelEndpointRelDB> listEndpointsByModelName(String modelName) {
        return db.selectFrom(MODEL_ENDPOINT_REL)
                .where(MODEL_ENDPOINT_REL.MODEL_NAME.eq(modelName))
                .fetchInto(ModelEndpointRelDB.class);
    }

    public List<String> listModelNamesByEndpoint(String endpoint) {
        return db.selectFrom(MODEL_ENDPOINT_REL)
                .where(MODEL_ENDPOINT_REL.ENDPOINT.eq(endpoint))
                .fetch(MODEL_ENDPOINT_REL.MODEL_NAME);
    }

    public List<ModelEndpointRelDB> listEndpointsByModelNames(Set<String> modelNames) {
        return db.selectFrom(MODEL_ENDPOINT_REL)
                .where(MODEL_ENDPOINT_REL.MODEL_NAME.in(modelNames))
                .fetchInto(ModelEndpointRelDB.class);
    }

    @Transactional
    public int batchDeleteModelAuthorizers(List<Long> ids) {
        return db.deleteFrom(MODEL_AUTHORIZER_REL)
                .where(MODEL_AUTHORIZER_REL.ID.in(ids))
                .execute();
    }

    @Transactional
    public int batchInsertModelAuthorizers(String modelName, Collection<MetaDataOps.ModelAuthorizer> authorizers) {
        List<ModelAuthorizerRelRecord> records = new ArrayList<>();
        for (MetaDataOps.ModelAuthorizer authorizer : authorizers) {
            ModelAuthorizerRelRecord rec = MODEL_AUTHORIZER_REL.newRecord();
            rec.from(authorizer);
            rec.setModelName(modelName);
            fillCreatorInfo(rec);
            records.add(rec);
        }
        return batchInsert(db, records);
    }

    public List<ModelAuthorizerRelDB> listAuthorizersByModelName(String modelName) {
        return db.selectFrom(MODEL_AUTHORIZER_REL)
                .where(MODEL_AUTHORIZER_REL.MODEL_NAME.eq(modelName))
                .fetchInto(ModelAuthorizerRelDB.class);
    }

    public List<String> listModelNamesByAuthorizer(String authorizerType, String authorizerCode) {
        return db.selectFrom(MODEL_AUTHORIZER_REL)
                .where(MODEL_AUTHORIZER_REL.AUTHORIZER_TYPE.eq(authorizerType))
                .and(MODEL_AUTHORIZER_REL.AUTHORIZER_CODE.eq(authorizerCode))
                .fetch(MODEL_AUTHORIZER_REL.MODEL_NAME);
    }

    @Override
    public TableImpl<ModelRecord> table() {
        return MODEL;
    }

    @Override
    protected TableField<ModelRecord, String> uniqueKey() {
        return MODEL.MODEL_NAME;
    }

    @Override
    protected TableField<ModelRecord, String> statusFiled() {
        return MODEL.STATUS;
    }
}
