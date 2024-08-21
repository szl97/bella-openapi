package com.ke.bella.openapi.db.repo;

import com.ke.bella.openapi.protocol.apikey.ApikeyCondition;
import com.ke.bella.openapi.tables.pojos.ApiKeyDB;
import com.ke.bella.openapi.tables.records.ApiKeyRecord;
import org.apache.commons.lang3.StringUtils;
import org.jooq.SelectSeekStep1;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.ke.bella.openapi.Tables.API_KEY;
import static com.ke.bella.openapi.db.TableConstants.ORG;
import static com.ke.bella.openapi.db.TableConstants.PERSON;

@Component
public class ApikeyRepo extends StatusRepo<ApiKeyDB, ApiKeyRecord, String> implements AutogenCodeRepo<ApiKeyRecord> {

    public List<ApiKeyDB> listAccessKeys(ApikeyCondition op) {
        return constructSql(op).fetchInto(ApiKeyDB.class);
    }

    public Page<ApiKeyDB> pageAccessKeys(ApikeyCondition op) {
        return queryPage(db, constructSql(op), op.getPageNum(), op.getPageSize(), ApiKeyDB.class);
    }

    private SelectSeekStep1<ApiKeyRecord, Long> constructSql(ApikeyCondition op) {
        return db.selectFrom(API_KEY)
                .where(StringUtils.isEmpty(op.getOwnerType()) ? DSL.noCondition() : API_KEY.OWNER_TYPE.eq(op.getOwnerType()))
                .and(StringUtils.isEmpty(op.getOwnerCode()) ? DSL.noCondition() : API_KEY.OWNER_CODE.eq(op.getOwnerCode()))
                .and(StringUtils.isEmpty(op.getParentCode()) ? DSL.noCondition() : API_KEY.PARENT_CODE.eq(op.getParentCode()))
                .and(op.getUserId() == null ? DSL.noCondition() : API_KEY.USER_ID.eq(op.getUserId()))
                .and(op.isIncludeChild() ? DSL.noCondition() : API_KEY.PARENT_CODE.eq(StringUtils.EMPTY))
                .and(StringUtils.isEmpty(op.getStatus()) ? DSL.noCondition() : API_KEY.STATUS.eq(op.getStatus()))
                .and(op.getPersonalCode().equals("0") ? DSL.noCondition() : (API_KEY.OWNER_TYPE.eq(PERSON).and(API_KEY.OWNER_CODE.eq(op.getPersonalCode()))).or(
                        (API_KEY.OWNER_TYPE.eq(ORG).and(API_KEY.OWNER_CODE.in(op.getOrgCodes())))
                ))
                .orderBy(API_KEY.ID.desc());
    }

    @Override
    public TableField<ApiKeyRecord, String> autoCode() {
        return API_KEY.CODE;
    }

    @Override
    public String prefix() {
        return "ak-";
    }

    @Override
    protected TableField<ApiKeyRecord, String> statusFiled() {
        return API_KEY.STATUS;
    }

    @Override
    protected TableImpl<ApiKeyRecord> table() {
        return API_KEY;
    }

    @Override
    protected TableField<ApiKeyRecord, String> uniqueKey() {
        return API_KEY.CODE;
    }
}
