package com.ke.bella.openapi.console;

import com.ke.bella.openapi.BellaContext;
import com.ke.bella.openapi.Operator;
import com.ke.bella.openapi.annotations.BellaAPI;
import com.ke.bella.openapi.apikey.ApikeyCreateOp;
import com.ke.bella.openapi.apikey.ApikeyInfo;
import com.ke.bella.openapi.apikey.ApikeyOps;
import com.ke.bella.openapi.db.repo.Page;
import com.ke.bella.openapi.service.ApikeyService;
import com.ke.bella.openapi.tables.pojos.ApikeyDB;
import com.ke.bella.openapi.tables.pojos.ApikeyMonthCostDB;
import com.ke.bella.openapi.utils.DateTimeUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@BellaAPI
@RestController
@RequestMapping("/console/apikey")
public class ApikeyConsoleController {

    @Autowired
    private ApikeyService apikeyService;

    @GetMapping("/page")
    public Page<ApikeyDB> pageApikey(ApikeyOps.ApikeyCondition condition) {
        return apikeyService.pageApikey(condition);
    }

    @PostMapping("/apply")
    public String apply(@RequestBody ApikeyOps.ApplyOp op) {
        Operator operator = BellaContext.getOperator();
        op.setOwnerName(operator.getUserName());
        return apikeyService.apply(op);
    }

    @PostMapping("/create")
    public String create(@RequestBody ApikeyCreateOp op) {
        Assert.notNull(op.getMonthQuota(), "配额应不可为null");
        Assert.notNull(op.getSafetyLevel(), "安全等级不可为空");
        Assert.isTrue(StringUtils.isNotEmpty(op.getRoleCode()) || CollectionUtils.isNotEmpty(op.getPaths()), "权限不可为空");
        return apikeyService.createByParentCode(op);
    }

    @PostMapping("/inactivate")
    public boolean inactivate(@RequestBody ApikeyOps.CodeOp op) {
        apikeyService.changeStatus(op, false);
        return true;
    }

    @PostMapping("/activate")
    public boolean activate(@RequestBody ApikeyOps.CodeOp op) {
        apikeyService.changeStatus(op, true);
        return true;
    }

    @PostMapping("/reset")
    public String reset(@RequestBody ApikeyOps.CodeOp op) {
        return apikeyService.reset(op);
    }

    @PostMapping("/rename")
    public boolean rename(@RequestBody ApikeyOps.NameOp op) {
        apikeyService.rename(op);
        return true;
    }

    @PostMapping("/service/bind")
    public boolean bindService(@RequestBody ApikeyOps.ServiceOp op) {
        apikeyService.bindService(op);
        return true;
    }

    @PostMapping("/certify")
    public boolean certify(@RequestBody ApikeyOps.CertifyOp op) {
        apikeyService.certify(op);
        return true;
    }

    @PostMapping("/quota/update")
    public boolean updateQuota(@RequestBody ApikeyOps.QuotaOp op) {
        apikeyService.updateQuota(op);
        return true;
    }

    @PostMapping("/role/update")
    public boolean updateRole(@RequestBody ApikeyOps.RoleOp op) {
        apikeyService.updateRole(op);
        return true;
    }

    @GetMapping("/billings")
    public List<ApikeyMonthCostDB> queryBillings(@RequestParam String akCode) {
        return apikeyService.queryBillingsByAkCode(akCode);
    }

    @GetMapping("/balance/{akCode}")
    public Map<String, Object> getApiKeyBalance(@PathVariable String akCode) {
        String currentMonth = DateTimeUtils.getCurrentMonth();
        BigDecimal monthCost = apikeyService.loadCost(akCode, currentMonth);
        ApikeyInfo apiKeyInfo = apikeyService.queryByCode(akCode, true);
        Map<String, Object> result = new HashMap<>();
        result.put("akCode", akCode);
        result.put("month", currentMonth);
        result.put("cost", monthCost);
        result.put("quota", apiKeyInfo != null ? apiKeyInfo.getMonthQuota() : BigDecimal.ZERO);
        result.put("balance", apiKeyInfo != null ? apiKeyInfo.getMonthQuota().subtract(monthCost) : BigDecimal.ZERO);
        return result;
    }

    @GetMapping("/fetchByCode")
    public ApikeyInfo fetchByCode(@RequestParam("code") String code, @RequestParam(value = "onlyActive", required = false) boolean onlyActive) {
        return apikeyService.queryByCode(code, onlyActive);
    }
}
