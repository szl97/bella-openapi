package com.ke.bella.openapi.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ke.bella.openapi.common.StatusEnum;
import com.ke.bella.openapi.common.exception.BizParamCheckException;
import com.ke.bella.openapi.db.repo.TenantRepo;
import com.ke.bella.openapi.tenant.CreateTenantOp;
import com.ke.bella.openapi.tenant.Tenant;
import com.ke.bella.openapi.tables.records.TenantRecord;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * function: 租户管理
 *
 * @author chenhongliang001
 */
@Component
public class TenantService {

    public static final String DEFAULT_TENANT_CODE = "default";
    
    @Autowired
    private TenantRepo tenantRepo;

    /**
     * 创建租户
     */
    @Transactional(rollbackFor = Exception.class)
    public String createTenant(CreateTenantOp op) {
        if(StringUtils.isEmpty(op.getTenantCode())) {
            op.setTenantCode(generateTenantCode());
        }
        
        TenantRecord tenant = tenantRepo.queryTenantByTenantCode(op.getTenantCode());
        // 判断租户是否已经存在
        if(tenant != null) {
            throw new BizParamCheckException(String.format("租户编码:%s已经存在", op.getTenantCode()));
        }
        // 保存
        tenantRepo.createTenant(buildTenant(op));

        return op.getTenantCode();
    }

    /**
     * 生成租户编码
     */
    public String generateTenantCode() {
        return UUID.randomUUID().toString();
    }

    /**
     * 构建租户记录
     */
    private TenantRecord buildTenant(CreateTenantOp tenantCreateOp) {
        TenantRecord tenant = new TenantRecord();
        BeanUtils.copyProperties(tenantCreateOp, tenant);
        fillCreateTenantOperator(tenant, tenantCreateOp.getUserId());
        return tenant;
    }

    /**
     * 填充创建租户的操作者信息
     */
    public void fillCreateTenantOperator(TenantRecord tenantRecord, Long userId) {
        tenantRecord.setCuid(userId);
        tenantRecord.setMuid(userId);
    }

    /**
     * 根据租户编码查询租户
     */
    public Tenant queryTenantByTenantCode(String tenantCode) {
        TenantRecord tenantRecord = tenantRepo.queryTenantByTenantCode(tenantCode);
        if(tenantRecord == null) {
            return null;
        }
        return Tenant.builder()
                .tenantName(tenantRecord.getTenantName())
                .tenantCode(tenantRecord.getTenantCode())
                .ownerUid(tenantRecord.getOwnerUid())
                .build();
    }

    /**
     * 获取或创建默认租户
     */
    public String getOrCreateDefaultTenant() {
        TenantRecord tenantRecord = tenantRepo.queryTenantByTenantCode(DEFAULT_TENANT_CODE);
        if (tenantRecord != null) {
            return DEFAULT_TENANT_CODE;
        }
        
        // 创建默认租户
        TenantRecord defaultTenant = new TenantRecord();
        defaultTenant.setTenantCode(DEFAULT_TENANT_CODE);
        defaultTenant.setTenantName("默认租户");
        defaultTenant.setTenantDescription("系统默认租户");
        defaultTenant.setOwnerUid("0");
        defaultTenant.setCuid(0L);
        defaultTenant.setMuid(0L);
        defaultTenant.setStatus(StatusEnum.VALID.getCode());
        
        tenantRepo.createTenant(defaultTenant);
        return DEFAULT_TENANT_CODE;
    }

    /**
     * 列出所有租户
     */
    public List<Tenant> listTenants() {
        List<TenantRecord> tenantRecords = tenantRepo.listTenants();
        
        if(CollectionUtils.isEmpty(tenantRecords)) {
            return Collections.emptyList();
        }

        return tenantRecords.stream()
                .map(record -> Tenant.builder()
                        .tenantName(record.getTenantName())
                        .tenantCode(record.getTenantCode())
                        .ownerUid(record.getOwnerUid())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 根据多个租户编码查询租户
     */
    public List<Tenant> listTenants(List<String> tenantCodes) {
        List<TenantRecord> tenantRecords = tenantRepo.queryTenantsByTenantCodes(tenantCodes);
        
        if(CollectionUtils.isEmpty(tenantRecords)) {
            return Collections.emptyList();
        }

        return tenantRecords.stream()
                .map(record -> Tenant.builder()
                        .tenantName(record.getTenantName())
                        .tenantCode(record.getTenantCode())
                        .ownerUid(record.getOwnerUid())
                        .build())
                .collect(Collectors.toList());
    }
}
