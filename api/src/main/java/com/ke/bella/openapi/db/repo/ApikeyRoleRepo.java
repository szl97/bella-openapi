package com.ke.bella.openapi.db.repo;

import com.ke.bella.openapi.tables.pojos.ApiKeyRoleDB;
import com.ke.bella.openapi.tables.records.ApiKeyRoleRecord;
import org.jooq.TableField;
import org.jooq.impl.TableImpl;
import org.springframework.stereotype.Component;

import static com.ke.bella.openapi.Tables.API_KEY_ROLE;

@Component
public class ApikeyRoleRepo extends UniqueKeyRepo<ApiKeyRoleDB, ApiKeyRoleRecord, String> implements AutogenCodeRepo<ApiKeyRoleRecord> {

    @Override
    public TableField<ApiKeyRoleRecord, String> autoCode() {
        return API_KEY_ROLE.ROLE_CODE;
    }

    @Override
    public String prefix() {
        return "role-";
    }

    @Override
    protected TableImpl<ApiKeyRoleRecord> table() {
        return API_KEY_ROLE;
    }

    @Override
    protected TableField<ApiKeyRoleRecord, String> uniqueKey() {
        return API_KEY_ROLE.ROLE_CODE;
    }
}
