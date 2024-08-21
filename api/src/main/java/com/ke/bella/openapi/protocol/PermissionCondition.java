package com.ke.bella.openapi.protocol;

import lombok.Data;

import java.util.Set;

@Data
public class PermissionCondition extends PageCondition {
    private String personalCode;
    private Set<String> orgCodes;
}
