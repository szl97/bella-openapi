package com.ke.bella.openapi.service;

import com.ke.bella.openapi.db.repo.EndpointRepo;
import com.ke.bella.openapi.db.repo.Page;
import com.ke.bella.openapi.dto.Condition;
import com.ke.bella.openapi.dto.MetaDataOps;
import com.ke.bella.openapi.tables.pojos.OpenapiEndpointDB;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.ke.bella.openapi.db.TableConstants.ACTIVE;
import static com.ke.bella.openapi.db.TableConstants.INACTIVE;

/**
 * Author: Stan Sai Date: 2024/8/2 10:41 description:
 */
@Component
public class EndpointService {
    @Autowired
    private EndpointRepo endpointRepo;

    @Transactional
    public OpenapiEndpointDB createEndpoint(MetaDataOps.EndpointOp op) {
        endpointRepo.checkExist(op.getEndpoint(), false);
        return endpointRepo.insert(op);
    }

    @Transactional
    public void updateEndpoint(MetaDataOps.EndpointOp op) {
        endpointRepo.checkExist(op.getEndpoint(), true);
        endpointRepo.update(op, op.getEndpoint());
    }

    @Transactional
    public void changeStatus(String endpoint, boolean active) {
        endpointRepo.checkExist(endpoint, true);
        String status = active ? ACTIVE : INACTIVE;
        endpointRepo.updateStatus(endpoint, status);
    }

    public OpenapiEndpointDB getActiveByEndpoint(String endpoint) {
        return getActive(UniqueKeyQuery.builder()
                .endpoint(endpoint)
                .build());
    }

    public OpenapiEndpointDB getOne(UniqueKeyQuery query) {
        if(!StringUtils.isEmpty(query.getEndpoint())) {
            return endpointRepo.queryByUniqueKey(query.getEndpoint());
        }
        if(!StringUtils.isEmpty(query.getEndpointCode())) {
            return endpointRepo.queryByEndpointCode(query.getEndpointCode());
        }
        return null;
    }

    public OpenapiEndpointDB getActive(UniqueKeyQuery query) {
        OpenapiEndpointDB db = getOne(query);
        return db == null || db.getStatus().equals(INACTIVE) ? null : db;
    }

    public List<OpenapiEndpointDB> listByCondition(Condition.EndpointCondition condition) {
        return endpointRepo.list(condition);
    }

    public Page<OpenapiEndpointDB> pageByCondition(Condition.EndpointCondition condition) {
        return endpointRepo.page(condition);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UniqueKeyQuery {
        private String endpoint;
        private String endpointCode;
    }

}
