package com.ke.bella.openapi.controller;

import com.ke.bella.openapi.db.repo.Page;
import com.ke.bella.openapi.dto.Condition;
import com.ke.bella.openapi.dto.EndpointCategoryTree;
import com.ke.bella.openapi.service.CategoryService;
import com.ke.bella.openapi.service.ChannelService;
import com.ke.bella.openapi.service.EndpointService;
import com.ke.bella.openapi.service.ModelService;
import com.ke.bella.openapi.tables.pojos.CategoryDB;
import com.ke.bella.openapi.tables.pojos.ChannelDB;
import com.ke.bella.openapi.tables.pojos.EndpointDB;
import com.ke.bella.openapi.tables.pojos.ModelDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Author: Stan Sai Date: 2024/8/1 17:27 description:
 */
@RestController
public class MetadataController {
    @Autowired
    private EndpointService endpointService;
    @Autowired
    private ModelService modelService;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/v1/endpoint/list")
    public List<EndpointDB> listEndpoint(Condition.EndpointCondition condition) {
        return endpointService.listByCondition(condition);
    }

    @GetMapping("/v1/endpoint/page")
    public Page<EndpointDB> pageEndpoint(Condition.EndpointCondition condition) {
        return endpointService.pageByCondition(condition);
    }

    @GetMapping("/v1/endpoint/info/{code}")
    public EndpointDB getEndpoint(@PathVariable String code) {
        return endpointService.getOne(EndpointService.UniqueKeyQuery.builder().endpointCode(code).build());
    }

    @GetMapping("/v1/model/list")
    public List<ModelDB> listModel(Condition.ModelCondition condition) {
        return modelService.listByCondition(condition);
    }

    @GetMapping("/v1/model/page")
    public Page<ModelDB> pageModel(Condition.ModelCondition condition) {
        return modelService.pageByCondition(condition);
    }

    @GetMapping("/v1/model/info/{name}")
    public ModelDB getModel(@PathVariable String name) {
        return modelService.getOne(name);
    }

    @GetMapping("/v1/channel/list")
    public List<ChannelDB> listChannel(Condition.ChannelCondition condition) {
        return channelService.listByCondition(condition);
    }

    @GetMapping("/v1/channel/page")
    public Page<ChannelDB> pageChannel(Condition.ChannelCondition condition) {
        return channelService.pageByCondition(condition);
    }

    @GetMapping("/v1/channel/info/{code}")
    public ChannelDB getChannel(@PathVariable String code) {
        return channelService.getOne(code);
    }

    @GetMapping("/v1/category/list")
    public List<CategoryDB> listCategory(Condition.CategoryCondition condition) {
        return categoryService.listByCondition(condition);
    }

    @GetMapping("/v1/category/page")
    public Page<CategoryDB> pageCategory(Condition.CategoryCondition condition) {
        return categoryService.pageByCondition(condition);
    }

    @GetMapping("/v1/category/tree")
    public EndpointCategoryTree listTree(Condition.CategoryTreeCondition condition) {
        return categoryService.listTree(condition);
    }

}
