package com.ke.bella.openapi.endpoints;

import com.ke.bella.openapi.annotations.BellaAPI;
import com.ke.bella.openapi.db.repo.Page;
import com.ke.bella.openapi.protocol.apikey.ApikeyCondition;
import com.ke.bella.openapi.service.ApikeyService;
import com.ke.bella.openapi.tables.pojos.ApiKeyDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@BellaAPI
@RestController
@RequestMapping("/v1/apikey")
public class ApikeyController {
    @Autowired
    private ApikeyService apikeyService;
    @GetMapping("/page")
    public Page<ApiKeyDB> pageApikey(ApikeyCondition condition) {
       return apikeyService.pageApikey(condition);
    }
}
