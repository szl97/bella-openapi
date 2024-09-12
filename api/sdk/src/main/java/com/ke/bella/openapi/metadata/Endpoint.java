package com.ke.bella.openapi.metadata;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Endpoint {
    private String endpoint;
    private String endpointCode;
    private String endpointName;
    private String maintainerCode;
    private String maintainerName;
    private String status;
    private Long cuid;
    private String cuName;
    private Long muid;
    private String muName;
    private LocalDateTime ctime;
    private LocalDateTime mtime;
}
