package com.ke.bella.openapi.console;

import com.ke.bella.openapi.annotations.BellaAPI;
import com.ke.bella.openapi.service.ApikeyService;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@BellaAPI
@RestController
@RequestMapping("/console/apikey")
public class ApikeyConsoleController {
    @Autowired
    private ApikeyService apikeyService;
    @PostMapping("/apply")
    public String apply(@RequestBody ApikeyOps.ApplyOp op) {
        return apikeyService.apply(op);
    }

    @PostMapping("/reset")
    public String reset(@RequestBody ApikeyOps.CodeOp op) {
        return apikeyService.reset(op);
    }

    @PostMapping("/role/update")
    public Boolean updateRole(@RequestBody ApikeyOps.RoleOp op) {
        apikeyService.updateRole(op);
        return true;
    }

    @PostMapping("/certify")
    public Boolean certify(@RequestBody ApikeyOps.CertifyOp op) {
        apikeyService.certify(op);
        return true;
    }

    @PostMapping("/activate")
    public Boolean activate(@RequestBody ApikeyOps.CodeOp op) {
        apikeyService.changeStatus(op, true);
        return true;
    }

    @PostMapping("/inactivate")
    public Boolean inactivate(@RequestBody ApikeyOps.CodeOp op) {
        apikeyService.changeStatus(op, false);
        return true;
    }
}
