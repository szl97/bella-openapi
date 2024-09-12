package com.ke.bella.openapi.metadata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndpointCategoryTree {
    private String categoryCode;
    private String categoryName;
    private List<Endpoint> endpoints;
    private List<EndpointCategoryTree> children;

    public void addChild(EndpointCategoryTree tree) {
        if(children == null) {
            children = new ArrayList<>();
        }
        children.add(tree);
    }
}
