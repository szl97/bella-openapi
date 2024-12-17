package com.ke.bella.openapi.protocol.tts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ke.bella.openapi.protocol.UserRequest;
import lombok.Data;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TtsRequest implements UserRequest, Serializable{
    String user;
    String model;
    String input;
    String voice;
    @JsonProperty("response_format")
    String responseFormat;
    Double speed;
}
