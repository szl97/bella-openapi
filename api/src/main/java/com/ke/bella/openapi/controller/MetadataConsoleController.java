package com.ke.bella.openapi.controller;

import com.ke.bella.openapi.dto.MetaDataOps;
import com.ke.bella.openapi.tables.pojos.OpenapiChannelDB;
import com.ke.bella.openapi.tables.pojos.OpenapiEndpointDB;
import com.ke.bella.openapi.tables.pojos.OpenapiModelDB;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Author: Stan Sai Date: 2024/8/1 16:22 description:
 */
@RestController("/console")
public class MetadataConsoleController {
    @PostMapping("/endpoint")
    public OpenapiEndpointDB createEndpoint(@RequestBody MetaDataOps.EndpointOp op) {
        return null;
    }
    @PutMapping("/endpoint")
    public Boolean updateEndpoint(@RequestBody MetaDataOps.EndpointOp op) {
        return true;
    }

    @PostMapping("/endpoint/activate")
    public Boolean activateEndpoint(@RequestBody MetaDataOps.EndpointStatusOp op) {
        return true;
    }

    @PutMapping("/endpoint/inactivate")
    public Boolean inactivateEndpoint(@RequestBody MetaDataOps.EndpointStatusOp op) {
        return true;
    }

    @PostMapping("/model")
    public OpenapiEndpointDB createModel(@RequestBody MetaDataOps.ModelOp op) {
        return null;
    }

    @PutMapping("/model")
    public Boolean updateModel(@RequestBody MetaDataOps.ModelOp op) {
        return true;
    }

    @PostMapping("/model/activate")
    public Boolean activateModel(@RequestBody MetaDataOps.ModelStatusOp op) {
        return true;
    }

    @PutMapping("/model/inactivate")
    public Boolean inactivateModel(@RequestBody MetaDataOps.ModelStatusOp op) {
        return true;
    }

    @PostMapping("/model/publish")
    public Boolean publishModel(@RequestBody MetaDataOps.ModelVisibilityOp op) {
        return true;
    }

    @PutMapping("/model/publish/cancel")
    public Boolean cancelPublishModel(@RequestBody MetaDataOps.ModelVisibilityOp op) {
        return true;
    }

    @PostMapping("/channel")
    public OpenapiEndpointDB createChannel(@RequestBody MetaDataOps.ChannelCreateOp op) {
        return null;
    }

    @PutMapping("/channel")
    public Boolean updateChannel(@RequestBody MetaDataOps.ChannelUpdateOp op) {
        return true;
    }

    @PostMapping("/channel/activate")
    public Boolean activateChannel(@RequestBody MetaDataOps.ChannelStatusOp op) {
        return true;
    }

    @PutMapping("/channel/inactivate")
    public Boolean inactivateChannel(@RequestBody MetaDataOps.ChannelStatusOp op) {
        return true;
    }

    @PostMapping("/category")
    public OpenapiEndpointDB createCategory(@RequestBody MetaDataOps.CategoryOp op) {
        return null;
    }

    @PutMapping("/category")
    public Boolean updateCategory(@RequestBody MetaDataOps.CategoryOp op) {
        return true;
    }

    @PostMapping("/category/activate")
    public Boolean activateCategory(@RequestBody MetaDataOps.CategoryStatusOp op) {
        return true;
    }

    @PutMapping("/category/inactivate/")
    public Boolean inactivateCategory(@RequestBody MetaDataOps.CategoryStatusOp op) {
        return true;
    }

    @PostMapping("/endpoint/category")
    public OpenapiEndpointDB addCategoryWithEndpoint(@RequestBody MetaDataOps.EndpointCategoryOp op) {
        return null;
    }

    @DeleteMapping("/endpoint/category")
    public Boolean removeCategoryWithEndpoint(@RequestBody MetaDataOps.EndpointCategoryOp op) {
        return true;
    }

    @PostMapping("/endpoint/category/replace")
    public Boolean replaceCategoryWithEndpoint(@RequestBody MetaDataOps.EndpointUpdateCategoriesOp op) {
        return true;
    }
}
