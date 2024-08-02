package com.ke.bella.openapi.controller.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Author: Stan Sai Date: 2024/7/31 20:07 description:
 */
@Data
@SuperBuilder
@NoArgsConstructor
@ToString
public class BellaResponse<T> {
    private int code;
    private String message;
    private long timestamp;
    private T data;
    private String stacktrace;
}

