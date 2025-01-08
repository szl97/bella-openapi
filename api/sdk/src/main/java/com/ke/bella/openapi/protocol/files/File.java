package com.ke.bella.openapi.protocol.files;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class File {
    private String id;
    @Builder.Default
    private String object = "file";
    private Long bytes;
    @JsonProperty("created_at")
    private Long createdAt;
    private String filename;
    private String extension;
    /**
     * assistants
     * assistants_output
     * batch
     * batch_output
     * fine-tune
     * fine-tune-results
     * vision
     */
    private String purpose;
    private Boolean deleted;
    @Deprecated
    private String status;
    @Deprecated
    @JsonProperty("status_details")
    private String statusDetails;
    private String url;
    /**
     * subtype of mime_type. eg: image/jpeg
     */
    @JsonProperty("mime_type")
    private String mimeType;
    /**
     * type of mime_type. eg: image
     */
    private String type;
}
