package com.ke.bella.openapi.metadata;

import com.ke.bella.openapi.Operator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Objects;
import java.util.Set;

public class MetaDataOps {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class EndpointOp extends Operator {
        private String endpoint;
        private String endpointName;
        private String maintainerCode;
        private String maintainerName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EndpointStatusOp extends Operator {
        private String endpoint;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelOp extends Operator {
        private String modelName;
        private Set<String> endpoints;
        private String ownerType;
        private String ownerCode;
        private String ownerName;
        private String documentUrl;
        private String properties;
        private String features;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelAuthorizerOp extends Operator {
        private String model;
        private Set<ModelAuthorizer> authorizers;
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelAuthorizer extends Operator {
        private String  authorizerType;
        private String  authorizerCode;
        private String  authorizerName;

        @Override
        public boolean equals(Object object) {
            if(this == object)
                return true;
            if(object == null || getClass() != object.getClass())
                return false;
            ModelAuthorizer that = (ModelAuthorizer) object;
            return Objects.equals(authorizerType, that.authorizerType) && Objects.equals(authorizerCode, that.authorizerCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(authorizerType, authorizerCode);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelNameOp extends Operator {
        private String modelName;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class ModelStatusOp extends ModelNameOp {

    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class ModelVisibilityOp extends ModelNameOp {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChannelCreateOp extends Operator {
        private String entityType;
        private String entityCode;
        private String dataDestination;
        private String priority;
        private String protocol;
        private String supplier;
        private String url;
        private String channelInfo;
        private String priceInfo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChannelUpdateOp extends Operator {
        private String priority;
        private String channelCode;
        private String channelInfo;
        private String priceInfo;
    }

    @Data
    public static class ChannelStatusOp extends Operator {
        private String channelCode;
    }

    @NoArgsConstructor
    @Data
    @SuperBuilder
    @AllArgsConstructor
    public static class CategoryCreateOp extends Operator {
        private String parentCode;
        private String categoryName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryStatusOp extends Operator {
        private String categoryCode;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class EndpointCategoriesOp extends Operator {
        private String endpoint;
        private Set<String> categoryCodes;
    }
}
