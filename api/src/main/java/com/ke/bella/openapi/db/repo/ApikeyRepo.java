package com.ke.bella.openapi.db.repo;

import com.ke.bella.openapi.BellaContext;
import com.ke.bella.openapi.protocol.apikey.ApikeyCondition;
import com.ke.bella.openapi.tables.pojos.ApikeyDB;
import com.ke.bella.openapi.tables.records.ApikeyRecord;
import org.apache.commons.lang3.StringUtils;
import org.jooq.SelectSeekStep1;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.ke.bella.openapi.Tables.APIKEY;
import static com.ke.bella.openapi.Tables.APIKEY_ROLE;
import static com.ke.bella.openapi.db.TableConstants.ORG;
import static com.ke.bella.openapi.db.TableConstants.PERSON;

@Component
public class ApikeyRepo extends StatusRepo<ApikeyDB, ApikeyRecord, String> implements AutogenCodeRepo<ApikeyRecord> {

    public BellaContext.ApikeyInfo queryBySha(String sha) {
        return db.select(APIKEY.fields())
                .select(APIKEY_ROLE.PATH).from(APIKEY)
                .leftJoin(APIKEY_ROLE).on(APIKEY.ROLE_CODE.eq(APIKEY_ROLE.ROLE_CODE))
                .where(APIKEY.AK_SHA.eq(sha))
                .fetchOneInto(BellaContext.ApikeyInfo.class);
    }

    public List<ApikeyDB> listAccessKeys(ApikeyCondition op) {
        return constructSql(op).fetchInto(ApikeyDB.class);
    }

    public Page<ApikeyDB> pageAccessKeys(ApikeyCondition op) {
        return queryPage(db, constructSql(op), op.getPageNum(), op.getPageSize(), ApikeyDB.class);
    }

    private SelectSeekStep1<ApikeyRecord, Long> constructSql(ApikeyCondition op) {
        return db.selectFrom(APIKEY)
                .where(StringUtils.isEmpty(op.getAkSha()) ? DSL.noCondition() : APIKEY.AK_SHA.eq(op.getAkSha()))
                .and(StringUtils.isEmpty(op.getOwnerType()) ? DSL.noCondition() : APIKEY.OWNER_TYPE.eq(op.getOwnerType()))
                .and(StringUtils.isEmpty(op.getOwnerCode()) ? DSL.noCondition() : APIKEY.OWNER_CODE.eq(op.getOwnerCode()))
                .and(StringUtils.isEmpty(op.getParentCode()) ? DSL.noCondition() : APIKEY.PARENT_CODE.eq(op.getParentCode()))
                .and(op.getUserId() == null ? DSL.noCondition() : APIKEY.USER_ID.eq(op.getUserId()))
                .and(op.isIncludeChild() ? DSL.noCondition() : APIKEY.PARENT_CODE.eq(StringUtils.EMPTY))
                .and(StringUtils.isEmpty(op.getStatus()) ? DSL.noCondition() : APIKEY.STATUS.eq(op.getStatus()))
                .and(StringUtils.isEmpty(op.getPersonalCode()) || op.getPersonalCode().equals("0") ? DSL.noCondition() : (APIKEY.OWNER_TYPE.eq(PERSON).and(APIKEY.OWNER_CODE.eq(op.getPersonalCode()))).or(
                        (APIKEY.OWNER_TYPE.eq(ORG).and(APIKEY.OWNER_CODE.in(op.getOrgCodes())))
                ))
                .orderBy(APIKEY.ID.desc());
    }

    @Override
    public TableField<ApikeyRecord, String> autoCode() {
        return APIKEY.CODE;
    }

    @Override
    public String prefix() {
        return "ak-";
    }

    @Override
    protected TableField<ApikeyRecord, String> statusFiled() {
        return APIKEY.STATUS;
    }

    @Override
    protected TableImpl<ApikeyRecord> table() {
        return APIKEY;
    }

    @Override
    protected TableField<ApikeyRecord, String> uniqueKey() {
        return APIKEY.CODE;
    }
}
