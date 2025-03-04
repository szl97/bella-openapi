package com.ke.bella.openapi.endpoints;

import com.ke.bella.openapi.Operator;
import com.ke.bella.openapi.annotations.BellaAPI;
import com.ke.bella.openapi.BellaContext;
import com.ke.bella.openapi.db.repo.UserRepo;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@BellaAPI
@RestController
@RequestMapping("/console/userInfo")
@Tag(name = "用户信息管理")
public class UserInfoController {
    @Autowired
    private UserRepo userRepo;

    @GetMapping
    public Operator whoami() {
        return BellaContext.getOperator();
    }

    @PostMapping("/manager")
    public Boolean addManager(@RequestBody Operator op) {
        Assert.hasText(op.getManagerAk(), "managerAk is required");
        Assert.hasText(op.getSource(), "source is required");
        Assert.isTrue(StringUtils.hasText(op.getEmail()) || StringUtils.hasText(op.getSourceId()), "sourceId or email is required");
        if(StringUtils.hasText(op.getSourceId())) {
            userRepo.addManagerBySourceAndSourceId(op.getSource(), op.getSourceId(), op.getManagerAk());
        } else {
            userRepo.addManagerBySourceAndEmail(op.getSource(), op.getEmail(), op.getManagerAk());
        }
        return true;
    }
}
