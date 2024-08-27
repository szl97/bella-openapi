package com.ke.bella.openapi.console;

import com.ke.bella.openapi.annotations.BellaAPI;
import com.ke.bella.openapi.service.ApikeyService;
import com.ke.bella.openapi.tables.pojos.ApiKeyMonthCostDB;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@BellaAPI
@RestController
@RequestMapping("/console/apikey")
public class ApikeyConsoleController {
    @Autowired
    private ApikeyService apikeyService;
    @PostMapping("/apply")
    public String apply(@RequestBody ApikeyOps.ApplyOp op) {
        Assert.isTrue(op.getMonthQuota() == null || op.getMonthQuota().doubleValue() > 0, "配额应大于0");
        return apikeyService.apply(op);
    }

    @PostMapping("/reset")
    public String reset(@RequestBody ApikeyOps.CodeOp op) {
        Assert.hasText(op.getCode(), "code不可为空");
        return apikeyService.reset(op);
    }

    @PostMapping("/role/update")
    public Boolean updateRole(@RequestBody ApikeyOps.RoleOp op) {
        Assert.hasText(op.getCode(), "code不可为空");
        Assert.isTrue(StringUtils.isNotEmpty(op.getRoleCode())
                        || CollectionUtils.isNotEmpty(op.getPaths()), "权限不可为空");
        apikeyService.updateRole(op);
        return true;
    }

    @PostMapping("/quota/update")
    public Boolean updateQuota(@RequestBody ApikeyOps.QuotaOp op) {
        Assert.hasText(op.getCode(), "code不可为空");
        Assert.notNull(op.getMonthQuota(), "配额不可为空");
        Assert.isTrue(op.getMonthQuota().doubleValue() > 0, "配额应大于0");
        apikeyService.updateQuota(op);
        return true;
    }

    @PostMapping("/certify")
    public Boolean certify(@RequestBody ApikeyOps.CertifyOp op) {
        Assert.hasText(op.getCode(), "code不可为空");
        Assert.notNull(op.getCertifyCode(), "认证码不可为空");
        apikeyService.certify(op);
        return true;
    }

    @PostMapping("/activate")
    public Boolean activate(@RequestBody ApikeyOps.CodeOp op) {
        Assert.hasText(op.getCode(), "code不可为空");
        apikeyService.changeStatus(op, true);
        return true;
    }

    @PostMapping("/inactivate")
    public Boolean inactivate(@RequestBody ApikeyOps.CodeOp op) {
        Assert.hasText(op.getCode(), "code不可为空");
        apikeyService.changeStatus(op, false);
        return true;
    }

    @GetMapping("/cost/{akCode}")
    public List<ApiKeyMonthCostDB> listApiKeyBillings(@PathVariable String akCode) {
        return apikeyService.queryBillingsByAkCode(akCode);
    }
}
