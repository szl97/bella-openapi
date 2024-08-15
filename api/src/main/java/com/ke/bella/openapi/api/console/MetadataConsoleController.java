package com.ke.bella.openapi.api.console;

import com.ke.bella.openapi.api.BellaAPI;
import com.ke.bella.openapi.dto.MetaDataOps;
import com.ke.bella.openapi.service.CategoryService;
import com.ke.bella.openapi.service.ChannelService;
import com.ke.bella.openapi.service.EndpointService;
import com.ke.bella.openapi.service.ModelService;
import com.ke.bella.openapi.tables.pojos.CategoryDB;
import com.ke.bella.openapi.tables.pojos.ChannelDB;
import com.ke.bella.openapi.tables.pojos.EndpointDB;
import com.ke.bella.openapi.tables.pojos.ModelDB;

import static com.ke.bella.openapi.api.console.MetadataValidator.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@BellaAPI
@RestController
@RequestMapping("/console")
public class MetadataConsoleController {
    @Autowired
    private EndpointService endpointService;
    @Autowired
    private ModelService modelService;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private CategoryService categoryService;

    @PostMapping("/endpoint")
    public EndpointDB createEndpoint(@RequestBody MetaDataOps.EndpointOp op) {
        checkEndpointOp(op, true);
        return endpointService.createEndpoint(op);
    }

    @PutMapping("/endpoint")
    public Boolean updateEndpoint(@RequestBody MetaDataOps.EndpointOp op) {
        checkEndpointOp(op, false);
        endpointService.updateEndpoint(op);
        return true;
    }

    @PostMapping("/endpoint/activate")
    public Boolean activateEndpoint(@RequestBody MetaDataOps.EndpointStatusOp op) {
        checkEndpointStatusOp(op);
        endpointService.changeStatus(op.getEndpoint(), true);
        return true;
    }

    @PostMapping("/endpoint/inactivate")
    public Boolean inactivateEndpoint(@RequestBody MetaDataOps.EndpointStatusOp op) {
        checkEndpointStatusOp(op);
        endpointService.changeStatus(op.getEndpoint(), false);
        return true;
    }

    @PostMapping("/model")
    public ModelDB createModel(@RequestBody MetaDataOps.ModelOp op) {
        checkModelOp(op, true);
        return modelService.createModel(op);
    }

    @PutMapping("/model")
    public Boolean updateModel(@RequestBody MetaDataOps.ModelOp op) {
        checkModelOp(op, false);
        modelService.updateModel(op);
        return true;
    }

    @PostMapping("/model/activate")
    public Boolean activateModel(@RequestBody MetaDataOps.ModelStatusOp op) {
        checkModelNameOp(op);
        modelService.changeStatus(op.getModelName(), true);
        return true;
    }

    @PostMapping("/model/inactivate")
    public Boolean inactivateModel(@RequestBody MetaDataOps.ModelStatusOp op) {
        checkModelNameOp(op);
        modelService.changeStatus(op.getModelName(), false);
        return true;
    }

    @PostMapping("/model/publish")
    public Boolean publishModel(@RequestBody MetaDataOps.ModelVisibilityOp op) {
        checkModelNameOp(op);
        modelService.changeVisibility(op.getModelName(), true);
        return true;
    }

    @PutMapping("/model/publish/cancel")
    public Boolean cancelPublishModel(@RequestBody MetaDataOps.ModelVisibilityOp op) {
        checkModelNameOp(op);
        modelService.changeVisibility(op.getModelName(), false);
        return true;
    }

    @PostMapping("/channel")
    public ChannelDB createChannel(@RequestBody MetaDataOps.ChannelCreateOp op) {
        checkChannelCreateOp(op);
        return channelService.createChannel(op);
    }

    @PutMapping("/channel")
    public Boolean updateChannel(@RequestBody MetaDataOps.ChannelUpdateOp op) {
        checkChannelUpdateOp(op);
        channelService.updateChannel(op);
        return true;
    }

    @PostMapping("/channel/activate")
    public Boolean activateChannel(@RequestBody MetaDataOps.ChannelStatusOp op) {
        checkChannelStatusOp(op);
        categoryService.changeStatus(op.getChannelCode(), true);
        return true;
    }

    @PostMapping("/channel/inactivate")
    public Boolean inactivateChannel(@RequestBody MetaDataOps.ChannelStatusOp op) {
        checkChannelStatusOp(op);
        categoryService.changeStatus(op.getChannelCode(), false);
        return true;
    }

    @PostMapping("/category")
    public CategoryDB createCategory(@RequestBody MetaDataOps.CategoryCreateOp op) {
        checkCategoryCreateOp(op);
        return categoryService.createCategory(op);
    }

    @PostMapping("/category/activate")
    public Boolean activateCategory(@RequestBody MetaDataOps.CategoryStatusOp op) {
        checkCategoryStatus(op);
        categoryService.changeStatus(op.getCategoryCode(), true);
        return true;
    }

    @PostMapping("/category/inactivate/")
    public Boolean inactivateCategory(@RequestBody MetaDataOps.CategoryStatusOp op) {
        checkCategoryStatus(op);
        categoryService.changeStatus(op.getCategoryCode(), false);
        return true;
    }

    @PostMapping("/endpoint/category")
    public Boolean addCategoryWithEndpoint(@RequestBody MetaDataOps.EndpointCategoriesOp op) {
        checkEndpointCategoryOp(op);
        categoryService.addCategoriesWithEndpoint(op);
        return true;
    }

    @DeleteMapping("/endpoint/category")
    public Boolean removeCategoryWithEndpoint(@RequestBody MetaDataOps.EndpointCategoriesOp op) {
        checkEndpointCategoryOp(op);
        categoryService.removeCategoriesWithEndpoint(op);
        return true;
    }

    @PostMapping("/endpoint/category/replace")
    public Boolean replaceCategoryWithEndpoint(@RequestBody MetaDataOps.EndpointCategoriesOp op) {
        checkReplaceEndpointCategoryOp(op);
        categoryService.replaceCategoryWithEndpoint(op);
        return true;
    }
}
