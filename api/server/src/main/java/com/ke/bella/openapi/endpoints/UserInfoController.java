package com.ke.bella.openapi.endpoints;

import com.ke.bella.openapi.BellaContext;
import com.ke.bella.openapi.Operator;
import com.ke.bella.openapi.annotations.BellaAPI;
import com.ke.bella.openapi.apikey.ApikeyInfo;
import com.ke.bella.openapi.common.exception.BizParamCheckException;
import com.ke.bella.openapi.db.repo.UserRepo;
import com.ke.bella.openapi.service.ApikeyService;
import com.ke.bella.openapi.tables.pojos.UserDB;
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
    @Autowired
    private ApikeyService apikeyService;

    @GetMapping
    public Operator whoami() {
        return BellaContext.getOperator();
    }

    @PostMapping("/manager")
    public UserDB addManager(@RequestBody Operator op) {
        Assert.hasText(op.getManagerAk(), "managerAk is required");
        Assert.isTrue((op.getUserId() != null && op.getUserId() > 0) ||(StringUtils.hasText(op.getSource()) && (StringUtils.hasText(op.getEmail()) || StringUtils.hasText(op.getSourceId()))),
                "invalid params");
        ApikeyInfo apikeyInfo = apikeyService.queryByCode(op.getManagerAk(), true);
        if(apikeyInfo == null) {
            throw new BizParamCheckException("apikey不存在");
        }
        UserDB user;
        if(op.getUserId() != null && op.getUserId() > 0) {
            user = userRepo.addManagerById(op.getUserId(), op.getManagerAk());
        } else if(StringUtils.hasText(op.getSourceId())) {
            user = userRepo.addManagerBySourceAndSourceId(op.getSource(), op.getSourceId(), op.getManagerAk());
        } else {
            user = userRepo.addManagerBySourceAndEmail(op.getSource(), op.getEmail(), op.getManagerAk());
        }
        if(user == null) {
            throw new BizParamCheckException("用户不存在");
        }
        return user;
    }
}
