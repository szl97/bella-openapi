package com.ke.bella.openapi.endpoints;

import com.ke.bella.openapi.BellaContext;
import com.ke.bella.openapi.annotations.BellaAPI;
import com.ke.bella.openapi.db.repo.Page;
import com.ke.bella.openapi.login.context.ConsoleContext;
import com.ke.bella.openapi.metadata.Condition;
import com.ke.bella.openapi.metadata.EndpointCategoryTree;
import com.ke.bella.openapi.metadata.EndpointDetails;
import com.ke.bella.openapi.service.CategoryService;
import com.ke.bella.openapi.service.ChannelService;
import com.ke.bella.openapi.service.EndpointService;
import com.ke.bella.openapi.service.ModelService;
import com.ke.bella.openapi.tables.pojos.CategoryDB;
import com.ke.bella.openapi.tables.pojos.ChannelDB;
import com.ke.bella.openapi.tables.pojos.EndpointDB;
import com.ke.bella.openapi.tables.pojos.ModelDB;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@BellaAPI
@RestController
@RequestMapping("/v1/meta")
@Tag(name = "信息查询")
public class MetadataController {
    @Autowired
    private EndpointService endpointService;
    @Autowired
    private ModelService modelService;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/endpoint/details")
    public EndpointDetails listEndpointDetails(Condition.EndpointDetailsCondition condition) {
        Assert.notNull(condition.getEndpoint(), "能力点不可为空");
        String identity = BellaContext.getApikeyIgnoreNull() == null ?
                ConsoleContext.getOperator().getUserId().toString() : BellaContext.getApikeyIgnoreNull().getCode();
        return endpointService.getEndpointDetails(condition, identity);
    }

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
        return modelService.listByConditionWithPermission(condition);
    }

    @GetMapping("/model/page")
    public Page<ModelDB> pageModel(Condition.ModelCondition condition) {
        return modelService.pageByConditionWithPermission(condition);
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

    @GetMapping("/category/tree/all")
    public List<EndpointCategoryTree> listAllTree() {
        return categoryService.listAllTree();
    }

    @GetMapping("/supplier/list")
    public List<String> listSuppliers() {
        return channelService.listSuppliers();
    }
}
