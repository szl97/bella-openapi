package com.ke.bella.openapi.protocol.embedding;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ke.bella.openapi.protocol.UserRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zhangxiaojia002
 * 
 * @date 2023/7/13 10:20 上午
 **/
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class EmbeddingRequest implements UserRequest, Serializable {
    private static final long serialVersionUID = 1L;
    private String user;
    private String model;
    private Object input;
    /**
     * Optional
     * Defaults to float
     * The format to return the embeddings in. Can be either float or base64.
     */
    @JsonProperty("encoding_format")
    private String encodingFormat;
    /**
     * Optional
     * The number of dimensions the resulting output embeddings should have.
     * Only supported in text-embedding-3 and later models.
     */
    private Integer dimensions;
}
