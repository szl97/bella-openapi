package com.ke.bella.openapi.metadata;

import com.ke.bella.openapi.BaseDto;
import com.ke.bella.openapi.protocol.IModelFeatures;
import com.ke.bella.openapi.protocol.IModelProperties;
import com.ke.bella.openapi.utils.JacksonUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Model extends BaseDto {
    private String modelName;
    private String documentUrl;
    private String visibility;
    private String ownerType;
    private String ownerCode;
    private String ownerName;
    private String status;
    private String properties;
    private String features;


    public <T extends IModelProperties> T toProperties(Class<T> type) {
        return JacksonUtils.deserialize(properties, type);
    }

    public <T extends IModelFeatures> T toFeatures(Class<T> type) {
        return JacksonUtils.deserialize(features, type);
    }
}
