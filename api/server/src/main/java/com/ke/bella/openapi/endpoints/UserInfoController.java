package com.ke.bella.openapi.endpoints;

import com.ke.bella.openapi.Operator;
import com.ke.bella.openapi.annotations.BellaAPI;
import com.ke.bella.openapi.login.context.ConsoleContext;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@BellaAPI
@RestController
@RequestMapping("/console/userInfo")
@Tag(name = "信息查询")
public class UserInfoController {
    @GetMapping
    public Operator whoami() {
        return ConsoleContext.getOperator();
    }
}
