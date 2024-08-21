package com.ke.bella.openapi.service;

import com.ke.bella.openapi.console.ApikeyOps;
import com.ke.bella.openapi.console.ConsoleContext;
import com.ke.bella.openapi.db.repo.ApikeyRepo;
import com.ke.bella.openapi.db.repo.ApikeyRoleRepo;
import com.ke.bella.openapi.db.repo.Page;
import com.ke.bella.openapi.protocol.PermissionCondition;
import com.ke.bella.openapi.protocol.apikey.ApikeyCondition;
import com.ke.bella.openapi.tables.pojos.ApiKeyDB;
import com.ke.bella.openapi.tables.pojos.ApiKeyRoleDB;
import com.ke.bella.openapi.utils.EncryptUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.ke.bella.openapi.db.TableConstants.ACTIVE;
import static com.ke.bella.openapi.db.TableConstants.INACTIVE;
import static com.ke.bella.openapi.db.TableConstants.ORG;
import static com.ke.bella.openapi.db.TableConstants.PERSON;
import static com.ke.bella.openapi.db.TableConstants.SYSTEM;

@Component
public class ApikeyService {
    @Autowired
    private ApikeyRepo apikeyRepo;
    @Autowired
    private ApikeyRoleRepo apikeyRoleRepo;

    public String apply(ApikeyOps.ApplyOp op) {
        String ak = UUID.randomUUID().toString();
        String sha = EncryptUtils.sha256(ak);
        String display = EncryptUtils.desensitize(ak);
        ApiKeyDB db = new ApiKeyDB();
        db.setAkSha(sha);
        db.setAkDisplay(display);
        db.setOwnerType(op.getOwnerType());
        db.setOwnerCode(op.getOwnerCode());
        db.setOwnerName(op.getOwnerName());
        apikeyRepo.insert(db);
        return ak;
    }

    public String reset(ApikeyOps.CodeOp op) {
        apikeyRepo.checkExist(op.getCode(), true);
        checkPermission(op.getCode());
        String ak = UUID.randomUUID().toString();
        String sha = EncryptUtils.sha256(ak);
        String display = EncryptUtils.desensitize(ak);
        ApiKeyDB db = new ApiKeyDB();
        db.setAkSha(sha);
        db.setAkDisplay(display);
        apikeyRepo.update(db, op.getCode());
        return ak;
    }

    public void updateRole(ApikeyOps.RoleOp op) {
        apikeyRepo.checkExist(op.getCode(), true);
        checkPermission(op.getCode());
        if(StringUtils.isNotEmpty(op.getRoleCode())) {
            apikeyRoleRepo.checkExist(op.getRoleCode(), true);
        } else {
            ApiKeyRoleDB roleDB = new ApiKeyRoleDB();
            roleDB.setPath(String.join(",", op.getPaths()));
            roleDB = apikeyRoleRepo.insert(roleDB);
            op.setRoleCode(roleDB.getRoleCode());
        }
        apikeyRepo.update(op, op.getCode());
    }

    public void certify(ApikeyOps.CertifyOp op) {
        apikeyRepo.checkExist(op.getCode(), true);
        checkPermission(op.getCode());
        Byte level = fetchLevelByCertifyCode(op.getCertifyCode());
        ApiKeyDB db = new ApiKeyDB();
        db.setCertifyCode(op.getCertifyCode());
        db.setSafetyLevel(level);
        apikeyRepo.update(db, op.getCode());
    }

    private Byte fetchLevelByCertifyCode(String code) {
        //todo: 根据验证码查询安全等级
        return 2;
    }

    public void updateQuota(ApikeyOps.QuotaOp op) {
        apikeyRepo.checkExist(op.getCode(), true);
        checkPermission(op.getCode());
        apikeyRepo.update(op, op.getCode());
    }

    public void changeStatus(ApikeyOps.CodeOp op, boolean active) {
        apikeyRepo.checkExist(op.getCode(), true);
        checkPermission(op.getCode());
        String status = active ? ACTIVE : INACTIVE;
        apikeyRepo.updateStatus(op.getCode(), status);
    }

    private void checkPermission(String code) {
        ConsoleContext.Operator operator = ConsoleContext.getOperator();
        if(operator.getUserId().equals(0L)) {
            return;
        }
        ApiKeyDB db = apikeyRepo.queryByUniqueKey(code);
        //todo: 获取所有 org
        Set<String> orgCodes = new HashSet<>();
        boolean isNotSystem = !db.getOwnerType().equals(SYSTEM);
        boolean isPersonOwner = db.getOwnerType().equals(PERSON) && db.getOwnerCode().equals(operator.getUserId().toString());
        boolean isOrgOwner = db.getOwnerType().equals(ORG) && orgCodes.contains(db.getOwnerCode());
        Assert.isTrue(isNotSystem && (isPersonOwner || isOrgOwner), "没有操作权限");
    }

    public Page<ApiKeyDB> pageApikey(ApikeyCondition condition) {
        fillPermissionCode(condition);
        return apikeyRepo.pageAccessKeys(condition);
    }

    public void fillPermissionCode(PermissionCondition condition) {
        ConsoleContext.Operator operator = ConsoleContext.getOperator();
        // TODO: 获取所有组织代码并填充到 orgCodes
        Set<String> orgCodes = new HashSet<>();

        if (StringUtils.isEmpty(condition.getPersonalCode())) {
            condition.setPersonalCode(operator.getUserId().toString());
        } else {
            validateUserPermission(operator, condition.getPersonalCode());
        }

        if (CollectionUtils.isEmpty(condition.getOrgCodes())) {
            condition.setOrgCodes(orgCodes);
        } else {
            validateOrgPermission(operator, condition.getOrgCodes(), orgCodes);
        }
    }

    private void validateUserPermission(ConsoleContext.Operator operator, String personalCode) {
        Assert.isTrue(operator.getUserId().equals(0L) || personalCode.equals(operator.getUserId().toString()), "没有查询权限");
    }

    private void validateOrgPermission(ConsoleContext.Operator operator, Set<String> conditionOrgCodes, Set<String> orgCodes) {
        Assert.isTrue(operator.getUserId().equals(0L) || CollectionUtils.isEmpty(conditionOrgCodes) || orgCodes.containsAll(conditionOrgCodes), "没有查询权限");
    }
}
