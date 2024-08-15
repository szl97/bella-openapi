package com.ke.bella.openapi.service;

import com.ke.bella.openapi.console.MetaDataOps;
import com.ke.bella.openapi.db.repo.EndpointRepo;
import com.ke.bella.openapi.db.repo.Page;
import com.ke.bella.openapi.protocol.Condition;
import com.ke.bella.openapi.tables.pojos.EndpointDB;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    public EndpointDB createEndpoint(MetaDataOps.EndpointOp op) {
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

    public EndpointDB getActiveByEndpoint(String endpoint) {
        return getActive(UniqueKeyQuery.builder()
                .endpoint(endpoint)
                .build());
    }

    public EndpointDB getOne(UniqueKeyQuery query) {
        if(StringUtils.isNotEmpty(query.getEndpoint())) {
            return endpointRepo.queryByUniqueKey(query.getEndpoint());
        }
        if(StringUtils.isNotEmpty(query.getEndpointCode())) {
            return endpointRepo.queryByEndpointCode(query.getEndpointCode());
        }
        return null;
    }

    public EndpointDB getActive(UniqueKeyQuery query) {
        EndpointDB db = getOne(query);
        return db == null || db.getStatus().equals(INACTIVE) ? null : db;
    }

    public List<EndpointDB> listByCondition(Condition.EndpointCondition condition) {
        return endpointRepo.list(condition);
    }

    public Page<EndpointDB> pageByCondition(Condition.EndpointCondition condition) {
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
