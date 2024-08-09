package com.ke.bella.openapi.db.repo;

import com.ke.bella.openapi.dto.Condition;
import com.ke.bella.openapi.tables.pojos.OpenapiEndpointDB;
import com.ke.bella.openapi.tables.records.OpenapiEndpointRecord;
import org.jooq.SelectSeekStep1;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.ke.bella.openapi.Tables.OPENAPI_ENDPOINT;

/**
 * Author: Stan Sai Date: 2024/8/1 19:55 description:
 */
@Component
public class EndpointRepo extends StatusRepo<OpenapiEndpointDB, OpenapiEndpointRecord, String> implements AutogenCodeRepo<OpenapiEndpointRecord> {

    public OpenapiEndpointDB queryByEndpointCode(String endpointCode) {
        return db.selectFrom(OPENAPI_ENDPOINT)
                .where(OPENAPI_ENDPOINT.ENDPOINT_CODE.eq(endpointCode))
                .fetchOneInto(OpenapiEndpointDB.class);
    }

    public List<OpenapiEndpointDB> list(Condition.EndpointCondition op) {
        return constructSql(op).fetchInto(OpenapiEndpointDB.class);
    }

    public Page<OpenapiEndpointDB> page(Condition.EndpointCondition op) {
        return queryPage(db, constructSql(op), op.getPageNum(), op.getPageSize(), OpenapiEndpointDB.class);
    }

    private SelectSeekStep1<OpenapiEndpointRecord, Long> constructSql(Condition.EndpointCondition op) {
        return db.selectFrom(OPENAPI_ENDPOINT)
                .where(StringUtils.isEmpty(op.getEndpoint()) ? DSL.noCondition() : OPENAPI_ENDPOINT.ENDPOINT.eq(op.getEndpoint()))
                .and(StringUtils.isEmpty(op.getEndpointCode()) ? DSL.noCondition() : OPENAPI_ENDPOINT.ENDPOINT_CODE.eq(op.getEndpointCode()))
                .and(StringUtils.isEmpty(op.getEndpointName())
                        ? DSL.noCondition()
                        : OPENAPI_ENDPOINT.ENDPOINT_NAME.like("%" + op.getEndpointName() + "%"))
                .and(StringUtils.isEmpty(op.getEndpoints()) ? DSL.noCondition() : OPENAPI_ENDPOINT.ENDPOINT.in(op.getEndpoints()))
                .and(StringUtils.isEmpty(op.getMaintainerCode()) ? DSL.noCondition() : OPENAPI_ENDPOINT.MAINTAINER_CODE.eq(op.getMaintainerCode()))
                .and(StringUtils.isEmpty(op.getMaintainerName())
                        ? DSL.noCondition()
                        : OPENAPI_ENDPOINT.MAINTAINER_NAME.like("%" + op.getMaintainerCode() + "%"))
                .and(StringUtils.isEmpty(op.getStatus()) ? DSL.noCondition() : OPENAPI_ENDPOINT.STATUS.eq(op.getStatus()))
                .orderBy(OPENAPI_ENDPOINT.ID.desc());
    }

    @Override
    public TableImpl<OpenapiEndpointRecord> table() {
        return OPENAPI_ENDPOINT;
    }

    @Override
    protected TableField<OpenapiEndpointRecord, String> uniqueKey() {
        return OPENAPI_ENDPOINT.ENDPOINT;
    }

    @Override
    protected TableField<OpenapiEndpointRecord, String> statusFiled() {
        return OPENAPI_ENDPOINT.STATUS;
    }

    @Override
    public TableField<OpenapiEndpointRecord, String> autoCode() {
        return OPENAPI_ENDPOINT.ENDPOINT_CODE;
    }
}
