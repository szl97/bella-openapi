package com.ke.bella.openapi.endpoints;

import com.ke.bella.openapi.annotations.BellaAPI;
import com.ke.bella.openapi.service.TenantService;
import com.ke.bella.openapi.tenant.CreateTenantOp;
import com.ke.bella.openapi.tenant.Tenant;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * function: 租户管理
 *
 * @author chenhongliang001
 */
@BellaAPI
@RestController
@RequestMapping("/v1/tenant")
@Tag(name = "租户管理")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @PostMapping("/create")
    public String createTenant(@RequestBody @Validated CreateTenantOp op) {
        return tenantService.createTenant(op);
    }

    @GetMapping("/get")
    public Tenant getTenant(@RequestParam String tenantCode) {
        return tenantService.queryTenantByTenantCode(tenantCode);
    }

    @GetMapping("/list")
    public List<Tenant> listTenants() {
        return tenantService.listTenants();
    }
    
    @GetMapping("/list/codes")
    public List<Tenant> listTenantsByCodes(@RequestParam List<String> tenantCodes) {
        return tenantService.listTenants(tenantCodes);
    }
}
