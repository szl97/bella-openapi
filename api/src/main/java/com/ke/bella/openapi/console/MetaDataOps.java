package com.ke.bella.openapi.console;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MetaDataOps {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class EndpointOp {
        private String endpoint;
        private String endpointName;
        private String maintainerCode;
        private String maintainerName;
    }

    @Data
    public static class EndpointStatusOp {
        private String endpoint;
    }

    @Data
    public static class ModelOp {
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
    public static class ModelAuthorizerOp {
        private String model;
        private Set<ModelAuthorizer> authorizers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelAuthorizer {
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
    public static class ModelNameOp {
        private String modelName;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ModelStatusOp extends ModelNameOp {

    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ModelVisibilityOp extends ModelNameOp {
    }

    @Data
    public static class ChannelCreateOp {
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
    public static class ChannelUpdateOp {
        private String priority;
        private String channelCode;
        private String channelInfo;
        private String priceInfo;
    }

    @Data
    public static class ChannelStatusOp {
        private String channelCode;
    }

    @NoArgsConstructor
    @Data
    @SuperBuilder
    public static class CategoryCreateOp {
        private String parentCode;
        private String categoryName;
    }

    @Data
    public static class CategoryStatusOp {
        private String categoryCode;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class EndpointCategoriesOp {
        private String endpoint;
        private Set<String> categoryCodes;
    }
}
