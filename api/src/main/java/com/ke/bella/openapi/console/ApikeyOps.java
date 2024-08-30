package com.ke.bella.openapi.console;

import com.ke.bella.openapi.protocol.PageCondition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

public class ApikeyOps {
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @SuperBuilder
    public static class ApplyOp extends ConsoleContext.Operator {
        private String name;
        private String ownerType;
        private String ownerCode;
        private String ownerName;
        private String roleCode;
        private BigDecimal monthQuota;
        private String remark;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NameOp extends ConsoleContext.Operator {
        private String code;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceOp extends ConsoleContext.Operator {
        private String code;
        private String serviceId;
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleOp extends ConsoleContext.Operator {
        private String code;
        private String roleCode;
        private List<String> paths;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertifyOp extends ConsoleContext.Operator {
        private String code;
        private String certifyCode;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuotaOp extends ConsoleContext.Operator {
        private String code;
        private BigDecimal monthQuota;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CodeOp extends ConsoleContext.Operator {
        private String code;
    }

    @Data
    public class ApikeyCondition extends PageCondition {
        private String ownerType;
        private String ownerCode;
        private String parentCode;
        private String name;
        private String serviceId;
        private String outEntityCode;
        private boolean includeChild;
        private String status;
    }
}
