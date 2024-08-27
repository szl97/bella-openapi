package com.ke.bella.openapi.service;

import static com.ke.bella.openapi.db.TableConstants.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.ke.bella.openapi.tables.pojos.ApikeyDB;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.alicp.jetcache.anno.CachePenetrationProtect;
import com.alicp.jetcache.anno.CacheUpdate;
import com.alicp.jetcache.anno.Cached;
import com.google.common.collect.Sets;
import com.ke.bella.openapi.BellaContext;
import com.ke.bella.openapi.console.ApikeyOps;
import com.ke.bella.openapi.db.repo.ApikeyCostRepo;
import com.ke.bella.openapi.db.repo.ApikeyRepo;
import com.ke.bella.openapi.db.repo.ApikeyRoleRepo;
import com.ke.bella.openapi.db.repo.Page;
import com.ke.bella.openapi.protocol.ChannelException;
import com.ke.bella.openapi.protocol.PermissionCondition;
import com.ke.bella.openapi.protocol.apikey.ApikeyCondition;
import com.ke.bella.openapi.protocol.apikey.ApikeyCreateOp;
import com.ke.bella.openapi.tables.pojos.ApikeyMonthCostDB;
import com.ke.bella.openapi.tables.pojos.ApikeyRoleDB;
import com.ke.bella.openapi.utils.EncryptUtils;
import com.ke.bella.openapi.utils.JacksonUtils;
import com.ke.bella.openapi.utils.MatchUtils;

@Component
public class ApikeyService {
    @Autowired
    private ApikeyRepo apikeyRepo;

    @Autowired
    private ApikeyRoleRepo apikeyRoleRepo;

    @Autowired
    private ApikeyCostRepo apikeyCostRepo;

    @Value("${apikey.basic.monthQuota:200}")
    private int basicMonthQuota;

    @Value("${apikey.basic.roleCode:low}")
    private String basicRoleCode;

    @Value("#{'${apikey.basic.childRoleCodes:low,high}'.split (',')}")
    private List<String> childRoleCodes;

    @Transactional
    public String apply(ApikeyOps.ApplyOp op) {
        String ak = UUID.randomUUID().toString();
        String sha = EncryptUtils.sha256(ak);
        String display = EncryptUtils.desensitize(ak);
        if(StringUtils.isNotEmpty(op.getRoleCode())) {
            Assert.isTrue(childRoleCodes.contains(op.getRoleCode()), "role code不可使用");
        }
        ApikeyDB db = new ApikeyDB();
        db.setAkSha(sha);
        db.setAkDisplay(display);
        db.setOwnerType(op.getOwnerType());
        db.setOwnerCode(op.getOwnerCode());
        db.setOwnerName(op.getOwnerName());
        db.setRoleCode(StringUtils.isEmpty(op.getRoleCode()) ? basicRoleCode : op.getRoleCode());
        db.setMonthQuota(op.getMonthQuota() == null ? BigDecimal.valueOf(basicMonthQuota) : op.getMonthQuota());
        apikeyRepo.insert(db);
        return ak;
    }

    public void sync(ApikeyDB ApikeyDB) {
        apikeyRepo.insert(ApikeyDB);
    }

    @Transactional
    public String createByParentCode(ApikeyCreateOp op) {
        BellaContext.ApikeyInfo apikey = BellaContext.getApikey();
        if(!apikey.getCode().equals(op.getParentCode())) {
            throw new ChannelException.AuthorizationException("没有操作权限");
        }
        if(StringUtils.isNotEmpty(op.getRoleCode())) {
            apikeyRoleRepo.checkExist(op.getRoleCode(), true);
        }
        Assert.isTrue(op.getMonthQuota() == null || op.getMonthQuota().doubleValue() <= apikey.getMonthQuota().doubleValue(), "配额超出ak的最大配额");
        Assert.isTrue(op.getSafetyLevel() <= apikey.getSafetyLevel(), "安全等级超出ak的最高等级");
        String ak = UUID.randomUUID().toString();
        String sha = EncryptUtils.sha256(ak);
        String display = EncryptUtils.desensitize(ak);
        ApikeyDB db = new ApikeyDB();
        db.setAkSha(sha);
        db.setAkDisplay(display);
        db.setParentCode(op.getParentCode());
        db.setUserId(op.getUserId());
        db.setOwnerType(apikey.getOwnerType());
        db.setOwnerCode(apikey.getOwnerCode());
        db.setOwnerName(apikey.getOwnerName());
        db.setRoleCode(op.getRoleCode());
        db.setMonthQuota(op.getMonthQuota());
        db = apikeyRepo.insert(db);
        if(CollectionUtils.isNotEmpty(op.getPaths())) {
           boolean match = op.getPaths().stream().allMatch(url -> apikey.getRolePath().getIncluded().stream().anyMatch(pattern -> MatchUtils.mathUrl(pattern, url))
                    && apikey.getRolePath().getExcluded().stream().noneMatch(pattern -> MatchUtils.mathUrl(pattern, url)));
            Assert.isTrue(match, "超出ak的权限范围");
            updateRole(ApikeyOps.RoleOp.builder().code(db.getCode()).paths(op.getPaths()).build());
        }
        return ak;
    }

    @Transactional
    public String reset(ApikeyOps.CodeOp op) {
        apikeyRepo.checkExist(op.getCode(), true);
        checkPermission(op.getCode());
        String ak = UUID.randomUUID().toString();
        String sha = EncryptUtils.sha256(ak);
        String display = EncryptUtils.desensitize(ak);
        ApikeyDB db = new ApikeyDB();
        db.setAkSha(sha);
        db.setAkDisplay(display);
        apikeyRepo.update(db, op.getCode());
        return ak;
    }

    @Transactional
    public void updateRole(ApikeyOps.RoleOp op) {
        apikeyRepo.checkExist(op.getCode(), true);
        checkPermission(op.getCode());
        if(StringUtils.isNotEmpty(op.getRoleCode())) {
            apikeyRoleRepo.checkExist(op.getRoleCode(), true);
        } else {
            ApikeyRoleDB roleDB = new ApikeyRoleDB();
            ApikeyRoleRepo.RolePath rolePath = new ApikeyRoleRepo.RolePath();
            rolePath.setIncluded(op.getPaths());
            roleDB.setPath(JacksonUtils.serialize(rolePath));
            roleDB = apikeyRoleRepo.insert(roleDB);
            op.setRoleCode(roleDB.getRoleCode());
        }
        apikeyRepo.update(op, op.getCode());
    }

    @Transactional
    public void certify(ApikeyOps.CertifyOp op) {
        apikeyRepo.checkExist(op.getCode(), true);
        checkPermission(op.getCode());
        Byte level = fetchLevelByCertifyCode(op.getCertifyCode());
        ApikeyDB db = new ApikeyDB();
        db.setCertifyCode(op.getCertifyCode());
        db.setSafetyLevel(level);
        apikeyRepo.update(db, op.getCode());
    }

    private Byte fetchLevelByCertifyCode(String certifyCode) {
        //todo: 根据验证码查询安全等级
        return 2;
    }

    @Transactional
    public void updateQuota(ApikeyOps.QuotaOp op) {
        apikeyRepo.checkExist(op.getCode(), true);
        checkPermission(op.getCode());
        apikeyRepo.update(op, op.getCode());
    }

    @Transactional
    public void changeStatus(ApikeyOps.CodeOp op, boolean active) {
        apikeyRepo.checkExist(op.getCode(), true);
        checkPermission(op.getCode());
        String status = active ? ACTIVE : INACTIVE;
        apikeyRepo.updateStatus(op.getCode(), status);
    }

    public BellaContext.ApikeyInfo verify(String ak) {
        String sha = EncryptUtils.sha256(ak);
        BellaContext.ApikeyInfo apikeyInfo = apikeyRepo.queryBySha(sha);
        if(apikeyInfo == null) {
            throw new ChannelException.AuthorizationException("api key不存在");
        }
        return apikeyInfo;
    }

    @CacheUpdate(name = "apikey:cost:month:", key = "#akCode + ':' + #month", value = "#result")
    public BigDecimal recordCost(String akCode, String month, BigDecimal cost) {
        BigDecimal amount = apikeyCostRepo.queryCost(akCode, month);
        if(amount == null) {
            apikeyCostRepo.insert(akCode, month);
        }
        apikeyCostRepo.increment(akCode, month, cost);
        return apikeyCostRepo.queryCost(akCode, month);
    }

    @Cached(name = "apikey:cost:month:", key = "#akCode + ':' + #month", cacheNullValue = true, expire = 31 * 24
            * 3600, condition = "T(com.ke.bella.openapi.utils.DateTimeUtils).isCurrentMonth(#month)")
    @CachePenetrationProtect(timeout = 5)
    public BigDecimal loadCost(String akCode, String month) {
        return apikeyCostRepo.queryCost(akCode, month);
    }

    public List<ApikeyMonthCostDB> queryBillingsByAkCode(String akCode) {
        return apikeyCostRepo.queryByAkCode(akCode);
    }

    private void checkPermission(String code) {
        BellaContext.ApikeyInfo apikeyInfo = BellaContext.getApikey();
        if(apikeyInfo.getOwnerType().equals(SYSTEM)) {
            return;
        }
        ApikeyDB db = apikeyRepo.queryByUniqueKey(code);
        //todo: 获取所有 org
        Set<String> orgCodes = new HashSet<>();
        if(db.getOwnerType().equals(SYSTEM)) {
            throw new ChannelException.AuthorizationException("没有操作权限");
        }
        if(db.getOwnerType().equals(PERSON)) {
            validateUserPermission(apikeyInfo, db.getOwnerCode());
        }
        if(db.getOwnerType().equals(ORG)) {
            validateOrgPermission(apikeyInfo, Sets.newHashSet(db.getOwnerCode()), orgCodes);
        }
    }

    public Page<ApikeyDB> pageApikey(ApikeyCondition condition) {
        fillPermissionCode(condition);
        return apikeyRepo.pageAccessKeys(condition);
    }

    public void fillPermissionCode(PermissionCondition condition) {
        BellaContext.ApikeyInfo apikeyInfo = BellaContext.getApikey();
        // TODO: 获取所有组织代码并填充到 orgCodes
        Set<String> orgCodes = new HashSet<>();

        if (StringUtils.isEmpty(condition.getPersonalCode())) {
            if(apikeyInfo.getOwnerType().equals(PERSON)) {
                condition.setPersonalCode(apikeyInfo.getOwnerCode());
            }
        } else {
            validateUserPermission(apikeyInfo, condition.getPersonalCode());
        }

        if (CollectionUtils.isEmpty(condition.getOrgCodes())) {
            condition.setOrgCodes(orgCodes);
        } else {
            validateOrgPermission(apikeyInfo, condition.getOrgCodes(), orgCodes);
        }
    }

    private void validateUserPermission(BellaContext.ApikeyInfo apikeyInfo, String personalCode) {
        if(apikeyInfo.getOwnerType().equals(SYSTEM) || (apikeyInfo.getOwnerType().equals(PERSON) && personalCode.equals(apikeyInfo.getOwnerCode()))) {
            return;
        }
        throw new ChannelException.AuthorizationException("没有操作权限");
    }

    private void validateOrgPermission(BellaContext.ApikeyInfo apikeyInfo, Set<String> conditionOrgCodes, Set<String> orgCodes) {
        if(apikeyInfo.getOwnerType().equals(SYSTEM) || CollectionUtils.isEmpty(conditionOrgCodes) || orgCodes.containsAll(conditionOrgCodes)) {
            return;
        }
        throw new ChannelException.AuthorizationException("没有操作权限");
    }
}
