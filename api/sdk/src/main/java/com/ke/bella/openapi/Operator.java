package com.ke.bella.openapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Operator implements Serializable {
    private static final long serialVersionUID = 1L;
    Long userId;
    String userName;
    String email;
    Map<String, Object> optionalInfo = new HashMap<>();
}
