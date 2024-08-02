package com.ke.bella.openapi.dto;

import com.ke.bella.openapi.tables.pojos.OpenapiEndpointDB;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Stan Sai Date: 2024/8/2 15:45 description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndpointCategoryTree {
    private String categoryCode;
    private String categoryName;
    private List<OpenapiEndpointDB> endpoints;
    private List<EndpointCategoryTree> children;

    public void addChild(EndpointCategoryTree tree) {
        if(children == null) {
            children = new ArrayList<>();
        }
        children.add(tree);
    }

}
