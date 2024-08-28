package com.ke.bella.openapi.endpoints;

import com.ke.bella.openapi.BellaContext;
import com.ke.bella.openapi.annotations.BellaAPI;
import com.ke.bella.openapi.db.repo.Page;
import com.ke.bella.openapi.protocol.apikey.ApikeyCondition;
import com.ke.bella.openapi.protocol.apikey.ApikeyCreateOp;
import com.ke.bella.openapi.service.ApikeyService;
import com.ke.bella.openapi.tables.pojos.ApikeyDB;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@BellaAPI
@RestController
@RequestMapping("/v1/apikey")
public class ApikeyController {
    @Autowired
    private ApikeyService apikeyService;
    @GetMapping("/page")
    public Page<ApikeyDB> pageApikey(ApikeyCondition condition) {
       return apikeyService.pageApikey(condition);
    }

    @PostMapping("/create")
    public String createApikey(@RequestBody ApikeyCreateOp op) {
        Assert.hasText(op.getParentCode(), "父ak不可为空");
        Assert.notNull(op.getUserId(), "userId不可为空");
        Assert.isTrue(op.getMonthQuota() == null || op.getMonthQuota().doubleValue() > 0, "配额应大于0");
        Assert.notNull(op.getSafetyLevel(), "安全等级不可为空");
        Assert.isTrue(StringUtils.isNotEmpty(op.getRoleCode())
                        || CollectionUtils.isNotEmpty(op.getPaths()), "权限不可为空");
        return apikeyService.createByParentCode(op);
    }

    @GetMapping("/whoAmI")
    public BellaContext.ApikeyInfo whoAmI() {
        return BellaContext.getApikey();
    }
}
