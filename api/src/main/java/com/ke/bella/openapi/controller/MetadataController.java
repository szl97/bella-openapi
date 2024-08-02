package com.ke.bella.openapi.controller;

import com.ke.bella.openapi.db.repo.Page;
import com.ke.bella.openapi.dto.Condition;
import com.ke.bella.openapi.dto.EndpointCategoryTree;
import com.ke.bella.openapi.tables.pojos.OpenapiCategoryDB;
import com.ke.bella.openapi.tables.pojos.OpenapiChannelDB;
import com.ke.bella.openapi.tables.pojos.OpenapiEndpointDB;
import com.ke.bella.openapi.tables.pojos.OpenapiModelDB;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Author: Stan Sai Date: 2024/8/1 17:27 description:
 */
@RestController
public class MetadataController {
    @GetMapping("/v1/endpoint/list")
    public List<OpenapiEndpointDB> listModel(Condition.EndpointCondition condition) {
        return null;
    }

    @GetMapping("/v1/endpoint/page")
    public Page<OpenapiEndpointDB> pageModel(Condition.EndpointCondition condition) {
        return null;
    }

    @GetMapping("/v1/endpoint/info/{id}")
    public OpenapiEndpointDB getEndpoint(@PathVariable Long id) {
        return null;
    }

    @GetMapping("/v1/model/list")
    public List<OpenapiModelDB> listModel(Condition.ModelCondition condition) {
        return null;
    }

    @GetMapping("/v1/model/page")
    public Page<OpenapiModelDB> pageModel(Condition.ModelCondition condition) {
        return null;
    }

    @GetMapping("/v1/model/info/{id}")
    public OpenapiModelDB getModel(@PathVariable Long id) {
        return null;
    }

    @GetMapping("/v1/channel/list")
    public List<OpenapiChannelDB> listChannel(Condition.ChannelCondition condition) {
        return null;
    }

    @GetMapping("/v1/channel/page")
    public Page<OpenapiChannelDB> pageChannel(Condition.ChannelCondition condition) {
        return null;
    }

    @GetMapping("/v1/channel/info/{id}")
    public OpenapiChannelDB getChannel(@PathVariable Long id) {
        return null;
    }

    @GetMapping("/v1/category/list")
    public List<OpenapiCategoryDB> listCategory(Condition.CategoryCondition condition) {
        return null;
    }

    @GetMapping("/v1/category/page")
    public Page<OpenapiCategoryDB> pageCategory(Condition.CategoryCondition condition) {
        return null;
    }

    @GetMapping("/v1/category/tree")
    public EndpointCategoryTree listTree(Condition.CategoryTreeCondition condition) {
        return null;
    }

}
