package com.ke.bella.openapi.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Author: Stan Sai Date: 2024/8/2 16:52 description:
 */
public class MetaDataOps {

    @Data
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
        private String documentUrl;
        private String properties;
        private String features;
    }

    @Data
    public static class ModelStatusOp {
        private String modelName;
    }

    @Data
    public static class ModelVisibilityOp {
        private String modelName;
    }


    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ChannelCreateOp extends ChannelUpdateOp {
        private String entityType;
        private String entityCode;
    }

    @Data
    public static class ChannelUpdateOp {
        private String channelName;
        private String dataDestination;
        private String priority;
        private String protocol;
        private String supplier;
        private String url;
        private String channelInfo;
        private String priceInfo;
    }

    @Data
    public static class ChannelStatusOp {
        private String channelName;
    }

    @Data
    public static class CategoryOp {
        private String categoryCode;
        private String categoryName;
        private String parentCode;
    }
    @Data
    public static class CategoryStatusOp {
        private String categoryCode;
    }

    @Data
    public static class EndpointCategoryOp {
        private String endpoint;
        private String categoryCode;
    }

    @Data
    public static class EndpointUpdateCategoriesOp {
        private String endpoint;
        private List<String> categoryCodes;
    }
}
