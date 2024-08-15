package com.ke.bella.openapi.db.repo;

import com.ke.bella.openapi.protocol.Condition;
import com.ke.bella.openapi.tables.pojos.ModelDB;
import com.ke.bella.openapi.tables.pojos.ModelEndpointRelDB;
import com.ke.bella.openapi.tables.records.ModelEndpointRelRecord;
import com.ke.bella.openapi.tables.records.ModelRecord;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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

import static com.ke.bella.openapi.Tables.MODEL;
import static com.ke.bella.openapi.Tables.MODEL_ENDPOINT_REL;

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
        return queryPage(db, constructSql(op), op.getPageNum(), op.getPageSize(), ModelDB.class);
    }

    private SelectSeekStep1<ModelRecord, Long> constructSql(Condition.ModelCondition op) {
        return db.selectFrom(MODEL)
                .where(StringUtils.isEmpty(op.getModelName()) ? DSL.noCondition() : MODEL.MODEL_NAME.like("%" + op.getModelName() + "%"))
                .and(CollectionUtils.isEmpty(op.getModelNames()) ? DSL.noCondition() : MODEL.MODEL_NAME.in(op.getModelNames()))
                .and(StringUtils.isEmpty(op.getVisibility()) ? DSL.noCondition() : MODEL.VISIBILITY.eq(op.getVisibility()))
                .and(StringUtils.isEmpty(op.getStatus()) ? DSL.noCondition() : MODEL.STATUS.eq(op.getStatus()))
                .orderBy(MODEL.ID.desc());
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
