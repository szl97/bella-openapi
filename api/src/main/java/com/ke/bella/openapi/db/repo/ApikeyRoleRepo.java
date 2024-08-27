package com.ke.bella.openapi.db.repo;

import com.ke.bella.openapi.tables.pojos.ApikeyRoleDB;
import com.ke.bella.openapi.tables.records.ApikeyRoleRecord;
import lombok.Data;
import org.jooq.TableField;
import org.jooq.impl.TableImpl;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.ke.bella.openapi.Tables.APIKEY_ROLE;

@Component
public class ApikeyRoleRepo extends UniqueKeyRepo<ApikeyRoleDB, ApikeyRoleRecord, String> implements AutogenCodeRepo<ApikeyRoleRecord> {

    @Override
    public TableField<ApikeyRoleRecord, String> autoCode() {
        return APIKEY_ROLE.ROLE_CODE;
    }

    @Override
    public String prefix() {
        return "role-";
    }

    @Override
    protected TableImpl<ApikeyRoleRecord> table() {
        return APIKEY_ROLE;
    }

    @Override
    protected TableField<ApikeyRoleRecord, String> uniqueKey() {
        return APIKEY_ROLE.ROLE_CODE;
    }

    @Data
    public static class RolePath {
        private List<String> included = new ArrayList<>();
        private List<String> excluded = new ArrayList<>();
    }
}
