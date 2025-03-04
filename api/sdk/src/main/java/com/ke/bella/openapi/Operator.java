package com.ke.bella.openapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Operator implements Serializable {
    private static final long serialVersionUID = 1L;
    protected Long userId;
    protected String userName;
    protected String email;
    protected String tenantId;
    protected String spaceCode;
    protected String source;
    protected String sourceId;
    protected String managerAk;
    protected Map<String, Object> optionalInfo = new HashMap<>();

    public String getSpaceCode() {
        return StringUtils.isEmpty(spaceCode) ? (userId > 0 ? String.valueOf(userId) : source + "_" + sourceId) : spaceCode;
    }
}
