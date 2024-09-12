package com.ke.bella.openapi.apikey;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ke.bella.openapi.utils.JacksonUtils;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ApikeyInfo {
    private String code;
    private String serviceId;
    private String akSha;
    private String akDisplay;
    private String name;
    private String outEntityCode;
    private String parentCode;
    private String ownerType;
    private String ownerCode;
    private String ownerName;
    private String roleCode;
    @JsonIgnore
    private String path;
    private Byte safetyLevel;
    private BigDecimal monthQuota;
    private RolePath rolePath;
    private String status;
    private String remark;

    public RolePath getRolePath() {
        if(path == null) {
            return new RolePath();
        }
        if(rolePath == null) {
            rolePath = JacksonUtils.deserialize(path, RolePath.class);
        }
        return rolePath;
    }

    @Data
    public static class RolePath {
        private List<String> included = new ArrayList<>();
        private List<String> excluded = new ArrayList<>();
    }
}
