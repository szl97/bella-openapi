package com.ke.bella.openapi.protocol.embedding;

import lombok.Data;

import java.util.List;

@Data
public class KeEmbeddingResponse {
    private int code;
    private String msg;
    private List<List<Double>> embed_res;
}
