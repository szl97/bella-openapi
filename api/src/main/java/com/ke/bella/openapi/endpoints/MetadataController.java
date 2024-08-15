package com.ke.bella.openapi.endpoints;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ke.bella.openapi.annotations.BellaAPI;
import com.ke.bella.openapi.db.repo.Page;
import com.ke.bella.openapi.protocol.Condition;
import com.ke.bella.openapi.protocol.EndpointCategoryTree;
import com.ke.bella.openapi.service.CategoryService;
import com.ke.bella.openapi.service.ChannelService;
import com.ke.bella.openapi.service.EndpointService;
import com.ke.bella.openapi.service.ModelService;
import com.ke.bella.openapi.tables.pojos.CategoryDB;
import com.ke.bella.openapi.tables.pojos.ChannelDB;
import com.ke.bella.openapi.tables.pojos.EndpointDB;
import com.ke.bella.openapi.tables.pojos.ModelDB;

@BellaAPI
@RestController
@RequestMapping("/v1/meta")
public class MetadataController {
    @Autowired
    private EndpointService endpointService;
    @Autowired
    private ModelService modelService;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/endpoint/list")
    public List<EndpointDB> listEndpoint(Condition.EndpointCondition condition) {
        return endpointService.listByCondition(condition);
    }

    @GetMapping("/endpoint/page")
    public Page<EndpointDB> pageEndpoint(Condition.EndpointCondition condition) {
        return endpointService.pageByCondition(condition);
    }

    @GetMapping("/endpoint/info/{code}")
    public EndpointDB getEndpoint(@PathVariable String code) {
        return endpointService.getOne(EndpointService.UniqueKeyQuery.builder().endpointCode(code).build());
    }

    @GetMapping("/model/list")
    public List<ModelDB> listModel(Condition.ModelCondition condition) {
        return modelService.listByCondition(condition);
    }

    @GetMapping("/model/page")
    public Page<ModelDB> pageModel(Condition.ModelCondition condition) {
        return modelService.pageByCondition(condition);
    }

    @GetMapping("/model/info/{name}")
    public ModelDB getModel(@PathVariable String name) {
        return modelService.getOne(name);
    }

    @GetMapping("/channel/list")
    public List<ChannelDB> listChannel(Condition.ChannelCondition condition) {
        return channelService.listByCondition(condition);
    }

    @GetMapping("/channel/page")
    public Page<ChannelDB> pageChannel(Condition.ChannelCondition condition) {
        return channelService.pageByCondition(condition);
    }

    @GetMapping("/channel/info/{code}")
    public ChannelDB getChannel(@PathVariable String code) {
        return channelService.getOne(code);
    }

    @GetMapping("/category/list")
    public List<CategoryDB> listCategory(Condition.CategoryCondition condition) {
        return categoryService.listByCondition(condition);
    }

    @GetMapping("/category/page")
    public Page<CategoryDB> pageCategory(Condition.CategoryCondition condition) {
        return categoryService.pageByCondition(condition);
    }

    @GetMapping("/category/tree")
    public EndpointCategoryTree listTree(Condition.CategoryTreeCondition condition) {
        return categoryService.listTree(condition);
    }

}
