package com.ke.bella.openapi.db.repo;

import static com.ke.bella.openapi.Tables.USER;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ke.bella.openapi.Operator;
import com.ke.bella.openapi.apikey.ApikeyInfo;
import com.ke.bella.openapi.common.EntityConstants;
import com.ke.bella.openapi.login.user.IUserRepo;
import com.ke.bella.openapi.tables.pojos.UserDB;
import com.ke.bella.openapi.tables.records.UserRecord;
import com.ke.bella.openapi.utils.EncryptUtils;
import com.ke.bella.openapi.utils.JacksonUtils;

@Component
public class UserRepo implements IUserRepo {
    private final DSLContext dsl;

    @Autowired
    private ApikeyRepo apikeyRepo;

    public UserRepo(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    @Transactional
    public Operator persist(Operator operator) {
        // 1. 通过 source 和 sourceId 查找用户
        UserRecord existingUser = dsl.selectFrom(USER)
                .where(USER.SOURCE.eq(operator.getSource())
                        .and(USER.SOURCE_ID.eq(operator.getSourceId())))
                .fetchOne();

        if (existingUser != null) {
            // 用数据库中的值替换实体中的值
            operator.setManagerAk(existingUser.getManagerAk());
            if (operator.getUserId() == null || operator.getUserId() <= 0) {
                operator.setUserId(existingUser.getId());
            }
            return operator;
        }

        // 2. 新用户
        UserRecord newUser = dsl.newRecord(USER);
        newUser.setUserName(operator.getUserName());
        newUser.setEmail(operator.getEmail());
        newUser.setSource(operator.getSource());
        newUser.setSourceId(operator.getSourceId());
        newUser.setManagerAk(operator.getManagerAk());
        newUser.setOptionalInfo(JacksonUtils.serialize(operator.getOptionalInfo()));

        newUser.store();
        
        // 不存在userId时，设置自增生成的 ID 为userId
        if (operator.getUserId() == null || operator.getUserId() <= 0) {
            operator.setUserId(newUser.getId());
        }
        
        return operator;
    }

    @Override
    public Operator checkSecret(String secret) {
        String sha = EncryptUtils.sha256(secret);
        ApikeyInfo apikeyInfo = apikeyRepo.queryBySha(sha);
        if(apikeyInfo == null || apikeyInfo.getStatus().equals(EntityConstants.INACTIVE)) {
            return null;
        }
        return Operator.builder()
                .userId(StringUtils.isNumeric(apikeyInfo.getOwnerCode()) ? Long.parseLong(apikeyInfo.getOwnerCode()) : 0L)
                .userName(apikeyInfo.getOwnerName())
                .managerAk(apikeyInfo.getCode())
                .optionalInfo(new HashMap<>())
                .sourceId(apikeyInfo.getOwnerCode())
                .source("secret")
                .build();
    }

    public UserDB addManagerById(Long id, String managerAk) {
        dsl.update(USER)
                .set(USER.MANAGER_AK, managerAk)
                .where(USER.ID.eq(id))
                .execute();
        return dsl.selectFrom(USER).where(USER.ID.eq(id)).fetchOneInto(UserDB.class);
    }

    public UserDB addManagerBySourceAndSourceId(String source, String sourceId, String managerAk) {
        dsl.update(USER)
                .set(USER.MANAGER_AK, managerAk)
                .where(USER.SOURCE_ID.eq(sourceId).and(USER.SOURCE.eq(source)))
                .execute();
        return dsl.selectFrom(USER).where(USER.SOURCE_ID.eq(sourceId).and(USER.SOURCE.eq(source))).fetchOneInto(UserDB.class);
    }

    public UserDB addManagerBySourceAndEmail(String source, String email, String managerAk) {
        dsl.update(USER)
                .set(USER.MANAGER_AK, managerAk)
                .where(USER.EMAIL.eq(email).and(USER.SOURCE.eq(source)))
                .execute();
        return dsl.selectFrom(USER).where(USER.EMAIL.eq(email).and(USER.SOURCE.eq(source))).fetchOneInto(UserDB.class);
    }
}
