package com.ke.bella.openapi.endpoints;

import com.ke.bella.openapi.BellaContext;
import com.ke.bella.openapi.annotations.BellaAPI;
import com.ke.bella.openapi.apikey.ApikeyCreateOp;
import com.ke.bella.openapi.apikey.ApikeyInfo;
import com.ke.bella.openapi.service.ApikeyService;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "信息查询")
public class ApikeyController {

    @Autowired
    private ApikeyService as;

    @PostMapping("/create")
    public String createApikey(@RequestBody ApikeyCreateOp op) {
        ApikeyInfo cur = BellaContext.getApikey();
        Assert.isTrue(StringUtils.isEmpty(cur.getParentCode()), "当前AK无创建子AK权限");
        Assert.isTrue(op.getMonthQuota() == null || op.getMonthQuota().doubleValue() > 0, "配额应大于0");
        Assert.notNull(op.getSafetyLevel(), "安全等级不可为空");
        Assert.isTrue(StringUtils.isNotEmpty(op.getRoleCode())
                        || CollectionUtils.isNotEmpty(op.getPaths()), "权限不可为空");

        op.setParentCode(cur.getCode());
        return as.createByParentCode(op);
    }

    @GetMapping("/whoami")
    public ApikeyInfo whoami() {
        return BellaContext.getApikey();
    }
}
