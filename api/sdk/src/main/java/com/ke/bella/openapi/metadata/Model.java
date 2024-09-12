package com.ke.bella.openapi.metadata;

import com.ke.bella.openapi.protocol.IModelFeatures;
import com.ke.bella.openapi.protocol.IModelProperties;
import com.ke.bella.openapi.utils.JacksonUtils;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Model {
    private String modelName;
    private String documentUrl;
    private String visibility;
    private String ownerType;
    private String ownerCode;
    private String ownerName;
    private String status;
    private String properties;
    private String features;
    private Long cuid;
    private String cuName;
    private Long muid;
    private String muName;
    private LocalDateTime ctime;
    private LocalDateTime mtime;

    public <T extends IModelProperties> T toProperties(Class<T> type) {
        return JacksonUtils.deserialize(properties, type);
    }

    public <T extends IModelFeatures> T toFeatures(Class<T> type) {
        return JacksonUtils.deserialize(features, type);
    }
}
