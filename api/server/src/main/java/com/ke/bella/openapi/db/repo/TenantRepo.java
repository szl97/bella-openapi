package com.ke.bella.openapi.db.repo;

import com.ke.bella.openapi.Tables;
import com.ke.bella.openapi.common.StatusEnum;
import com.ke.bella.openapi.tables.records.TenantRecord;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * function: 租户数据操作
 *
 * author chenhongliang001
 */
@Component
public class TenantRepo implements BaseRepo {

    @Resource
    private DSLContext db;

    /**
     * 根据租户编码查询租户
     */
    public TenantRecord queryTenantByTenantCode(String tenantCode) {
        return db.selectFrom(Tables.TENANT)
                .where(Tables.TENANT.TENANT_CODE.eq(tenantCode))
                .and(Tables.TENANT.STATUS.eq(StatusEnum.VALID.getCode()))
                .fetchOneInto(TenantRecord.class);
    }

    /**
     * 根据多个租户编码查询租户
     */
    public List<TenantRecord> queryTenantsByTenantCodes(List<String> tenantCodes) {
        return db.selectFrom(Tables.TENANT)
                .where(Tables.TENANT.TENANT_CODE.in(tenantCodes))
                .and(Tables.TENANT.STATUS.eq(StatusEnum.VALID.getCode()))
                .fetchInto(TenantRecord.class);
    }

    /**
     * 创建租户
     */
    @Transactional(rollbackFor = Exception.class)
    public void createTenant(TenantRecord record) {
        if(StringUtils.isEmpty(record.getTenantDescription())) {
            record.setTenantDescription("");
        }
        db.insertInto(Tables.TENANT).set(record).execute();
    }

    /**
     * 更新租户名称
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateTenantName(String tenantCode, String tenantName, Long muid) {
        db.update(Tables.TENANT)
                .set(Tables.TENANT.TENANT_NAME, tenantName)
                .set(Tables.TENANT.MUID, muid)
                .where(Tables.TENANT.TENANT_CODE.eq(tenantCode))
                .execute();
    }

    /**
     * 更新租户拥有者
     */
    @Transactional(rollbackFor = Exception.class)
    public void changeTenantOwner(String tenantCode, String ownerUid, Long muid) {
        db.update(Tables.TENANT)
                .set(Tables.TENANT.OWNER_UID, ownerUid)
                .set(Tables.TENANT.MUID, muid)
                .where(Tables.TENANT.TENANT_CODE.eq(tenantCode))
                .execute();
    }

    /**
     * 列出所有租户
     */
    public List<TenantRecord> listTenants() {
        return db.selectFrom(Tables.TENANT)
                .where(Tables.TENANT.STATUS.eq(StatusEnum.VALID.getCode()))
                .fetchInto(TenantRecord.class);
    }
}
