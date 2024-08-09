package com.ke.bella.openapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

/**
 * Author: Stan Sai Date: 2024/8/2 16:52 description:
 */
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
        private String documentUrl;
        private String properties;
        private String features;
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
        private String channelCode;
        private String channelInfo;
        private String priceInfo;
    }

    @Data
    public static class ChannelStatusOp {
        private String channelCode;
    }

    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    public static class CategoryCreateOp extends CategoryOp {
        private String parentCode;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    @Data
    public static class CategoryOp {
        private String categoryCode;
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
    public static class EndpointCategoryOp {
        private String endpoint;
        private String categoryCode;
    }

    @Data
    public static class EndpointUpdateCategoriesOp {
        private String endpoint;
        private Set<String> categoryCodes;
    }
}
