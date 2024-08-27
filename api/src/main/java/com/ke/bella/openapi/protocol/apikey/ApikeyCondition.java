package com.ke.bella.openapi.protocol.apikey;

import com.ke.bella.openapi.protocol.PageCondition;
import com.ke.bella.openapi.protocol.PermissionCondition;
import lombok.Data;

import java.util.Set;

@Data
public class ApikeyCondition extends PermissionCondition {
    private String akSha;
    private String ownerType;
    private String ownerCode;
    private String parentCode;
    private Long userId;
    private boolean includeChild;
    private String status;
}


