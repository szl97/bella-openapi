package com.ke.bella.openapi.protocol;

import lombok.Data;

@Data
public class PageCondition {
    private int pageNum = 1;
    private int pageSize = 10;
}
