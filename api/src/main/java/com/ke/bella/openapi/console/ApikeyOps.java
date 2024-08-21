package com.ke.bella.openapi.console;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

public class ApikeyOps {
    @Data
    public static class ApplyOp extends ConsoleContext.Operator {
        private String ownerType;
        private String ownerCode;
        private String ownerName;
    }

    @Data
    public static class RoleOp extends ConsoleContext.Operator {
        private String code;
        private String roleCode;
        private List<String> paths;
    }

    @Data
    public static class CertifyOp extends ConsoleContext.Operator {
        private String code;
        private String certifyCode;
    }

    @Data
    public static class QuotaOp extends ConsoleContext.Operator {
        private String code;
        private BigDecimal monthQuota;
    }

    @Data
    public static class CodeOp extends ConsoleContext.Operator {
        private String code;
    }
}
