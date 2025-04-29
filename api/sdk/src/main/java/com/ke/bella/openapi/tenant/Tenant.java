package com.ke.bella.openapi.tenant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * function: 租户信息
 *
 * @author chenhongliang001
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Tenant {

    private String tenantCode;

    private String tenantName;

    private String ownerUid;
}
