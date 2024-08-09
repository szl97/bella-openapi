package com.ke.bella.openapi.db.repo;

import com.ke.bella.openapi.dto.Condition;
import com.ke.bella.openapi.tables.pojos.OpenapiChannelDB;
import com.ke.bella.openapi.tables.records.OpenapiChannelRecord;
import org.jooq.SelectSeekStep1;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.ke.bella.openapi.tables.OpenapiChannel.OPENAPI_CHANNEL;

/**
 * Author: Stan Sai Date: 2024/8/1 20:51 description:
 */
@Component
public class ChannelRepo extends StatusRepo<OpenapiChannelDB, OpenapiChannelRecord, String> implements AutogenCodeRepo<OpenapiChannelRecord> {

    public List<OpenapiChannelDB> list(Condition.ChannelCondition op) {
        return constructSql(op).fetchInto(OpenapiChannelDB.class);
    }

    public Page<OpenapiChannelDB> page(Condition.ChannelCondition op) {
        return queryPage(db, constructSql(op), op.getPageNum(), op.getPageSize(), OpenapiChannelDB.class);
    }

    private SelectSeekStep1<OpenapiChannelRecord, Long> constructSql(Condition.ChannelCondition op) {
        return db.selectFrom(OPENAPI_CHANNEL)
                .where(StringUtils.isEmpty(op.getEntityType()) ? DSL.noCondition() : OPENAPI_CHANNEL.ENTITY_TYPE.eq(op.getEntityType()))
                .and(StringUtils.isEmpty(op.getEntityType()) ? DSL.noCondition() : OPENAPI_CHANNEL.ENTITY_CODE.eq(op.getEntityCode()))
                .and(StringUtils.isEmpty(op.getSupplier()) ? DSL.noCondition() : OPENAPI_CHANNEL.SUPPLIER.eq(op.getSupplier()))
                .and(StringUtils.isEmpty(op.getProtocol()) ? DSL.noCondition() : OPENAPI_CHANNEL.PROTOCOL.eq(op.getProtocol()))
                .and(StringUtils.isEmpty(op.getPriority()) ? DSL.noCondition() : OPENAPI_CHANNEL.PRIORITY.eq(op.getPriority()))
                .and(StringUtils.isEmpty(op.getDataDestination()) ? DSL.noCondition() : OPENAPI_CHANNEL.DATA_DESTINATION.eq(op.getDataDestination()))
                .and(StringUtils.isEmpty(op.getStatus()) ? DSL.noCondition() : OPENAPI_CHANNEL.STATUS.eq(op.getStatus()))
                .orderBy(OPENAPI_CHANNEL.ID.desc());
    }

    @Override
    public TableImpl<OpenapiChannelRecord> table() {
        return OPENAPI_CHANNEL;
    }

    @Override
    protected TableField<OpenapiChannelRecord, String> uniqueKey() {
        return OPENAPI_CHANNEL.CHANNEL_CODE;
    }

    @Override
    protected TableField<OpenapiChannelRecord, String> statusFiled() {
        return OPENAPI_CHANNEL.STATUS;
    }

    @Override
    public TableField<OpenapiChannelRecord, String> autoCode() {
        return OPENAPI_CHANNEL.CHANNEL_CODE;
    }
}
