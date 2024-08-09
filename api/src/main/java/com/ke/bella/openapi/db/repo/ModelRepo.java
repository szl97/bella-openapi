package com.ke.bella.openapi.db.repo;

import com.ke.bella.openapi.dto.Condition;
import com.ke.bella.openapi.tables.pojos.OpenapiModelDB;
import com.ke.bella.openapi.tables.pojos.OpenapiModelEndpointRelationDB;
import com.ke.bella.openapi.tables.records.OpenapiModelEndpointRelationRecord;
import com.ke.bella.openapi.tables.records.OpenapiModelRecord;
import org.jooq.SelectSeekStep1;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.ke.bella.openapi.Tables.OPENAPI_MODEL;
import static com.ke.bella.openapi.Tables.OPENAPI_MODEL_ENDPOINT_RELATION;
import static com.ke.bella.openapi.tables.OpenapiChannel.OPENAPI_CHANNEL;

/**
 * Author: Stan Sai Date: 2024/8/1 20:34 description:
 */
@Component
public class ModelRepo extends StatusRepo<OpenapiModelDB, OpenapiModelRecord, String> {

    public void updateVisibility(String modelName, String visibility) {
        OpenapiModelRecord rec = OPENAPI_MODEL.newRecord();
        rec.setVisibility(visibility);
        fillUpdatorInfo(rec);
        int num = db.update(OPENAPI_MODEL)
                .set(rec)
                .where(OPENAPI_MODEL.MODEL_NAME.eq(modelName))
                .execute();
        Assert.isTrue(num == 1, "模型实体更新失败，请检查模型实体是否存在");
    }

    public List<OpenapiModelDB> list(Condition.ModelCondition op) {
        return constructSql(op).fetchInto(OpenapiModelDB.class);
    }

    public Page<OpenapiModelDB> page(Condition.ModelCondition op) {
        return queryPage(db, constructSql(op), op.getPageNum(), op.getPageSize(), OpenapiModelDB.class);
    }

    private SelectSeekStep1<OpenapiModelRecord, Long> constructSql(Condition.ModelCondition op) {
        return db.selectFrom(OPENAPI_MODEL)
                .where(StringUtils.isEmpty(op.getModelName()) ? DSL.noCondition() : OPENAPI_MODEL.MODEL_NAME.like("%" + op.getModelName() + "%"))
                .and(CollectionUtils.isEmpty(op.getModelNames()) ? DSL.noCondition() : OPENAPI_MODEL.MODEL_NAME.in(op.getModelNames()))
                .and(StringUtils.isEmpty(op.getVisibility()) ? DSL.noCondition() : OPENAPI_MODEL.VISIBILITY.eq(op.getVisibility()))
                .and(StringUtils.isEmpty(op.getStatus()) ? DSL.noCondition() : OPENAPI_CHANNEL.STATUS.eq(op.getStatus()))
                .orderBy(OPENAPI_MODEL.ID.desc());
    }

    public int batchInsertModelEndpoints(String modelName, Collection<String> endpoints) {
        List<OpenapiModelEndpointRelationRecord> records = new ArrayList<>();
        for (String endpoint : endpoints) {
            OpenapiModelEndpointRelationRecord rec = OPENAPI_MODEL_ENDPOINT_RELATION.newRecord();
            rec.setModelName(modelName);
            rec.setEndpoint(endpoint);
            fillCreatorInfo(rec);
            records.add(rec);
        }
        return batchInsert(db, records);
    }

    public int batchDeleteModelEndpoints(List<Long> ids) {
        return db.deleteFrom(OPENAPI_MODEL_ENDPOINT_RELATION)
                .where(OPENAPI_MODEL_ENDPOINT_RELATION.ID.in(ids))
                .execute();
    }

    public List<OpenapiModelEndpointRelationDB> listEndpointsByModelName(String modelName) {
        return db.selectFrom(OPENAPI_MODEL_ENDPOINT_RELATION)
                .where(OPENAPI_MODEL_ENDPOINT_RELATION.MODEL_NAME.eq(modelName))
                .fetchInto(OpenapiModelEndpointRelationDB.class);
    }

    public List<String> listModelNamesByEndpoint(String endpoint) {
        return db.selectFrom(OPENAPI_MODEL_ENDPOINT_RELATION)
                .where(OPENAPI_MODEL_ENDPOINT_RELATION.ENDPOINT.eq(endpoint))
                .fetch(OPENAPI_MODEL_ENDPOINT_RELATION.MODEL_NAME);
    }

    @Override
    public TableImpl<OpenapiModelRecord> table() {
        return OPENAPI_MODEL;
    }

    @Override
    protected TableField<OpenapiModelRecord, String> uniqueKey() {
        return OPENAPI_MODEL.MODEL_NAME;
    }

    @Override
    protected TableField<OpenapiModelRecord, String> statusFiled() {
        return OPENAPI_MODEL.STATUS;
    }
}
