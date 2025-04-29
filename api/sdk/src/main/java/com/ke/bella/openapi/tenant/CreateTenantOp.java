package com.ke.bella.openapi.tenant;

import com.ke.bella.openapi.Operator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/**
 * function: 创建租户参数
 *
 * author chenhongliang001
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class CreateTenantOp extends Operator {

    /**
     * 租户名称
     */
    @NotEmpty(message = "tenantName不能为空")
    @Size(max = 128, message = "tenantName不能超过128个字符")
    private String tenantName;

    /**
     * 租户描述
     */
    @Size(max = 255, message = "tenantDescription不能超过255个字符")
    private String tenantDescription;

    /**
     * 租户编码
     */
    @Size(max = 64, message = "tenantCode不能超过64个字符")
    private String tenantCode;

    /**
     * 租户拥有者id
     */
    @NotEmpty(message = "ownerUid不能为空")
    @Size(max = 64, message = "ownerUid不能超过64个字符")
    private String ownerUid;
}
