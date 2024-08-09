package com.ke.bella.openapi.service;

import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ke.bella.openapi.db.repo.CategoryRepo;
import com.ke.bella.openapi.db.repo.Page;
import com.ke.bella.openapi.dto.Condition;
import com.ke.bella.openapi.dto.EndpointCategoryTree;
import com.ke.bella.openapi.dto.MetaDataOps;
import com.ke.bella.openapi.tables.pojos.OpenapiCategoryDB;
import com.ke.bella.openapi.tables.pojos.OpenapiEndpointCategoryRelationDB;
import com.ke.bella.openapi.tables.pojos.OpenapiEndpointDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ke.bella.openapi.db.TableConstants.ACTIVE;
import static com.ke.bella.openapi.db.TableConstants.INACTIVE;

/**
 * Author: Stan Sai Date: 2024/8/2 12:00 description:
 */
@Component
public class CategoryService {
    @Autowired
    private CategoryRepo categoryRepo;
    @Autowired
    private EndpointService endpointService;

    @Transactional
    public OpenapiCategoryDB createCategory(MetaDataOps.CategoryCreateOp op) {
        categoryRepo.checkExist(op.getCategoryCode(), false);
        checkParentCode(op.getParentCode());
        return categoryRepo.insert(op);
    }

    @Transactional
    public void updateCategory(MetaDataOps.CategoryOp op) {
        categoryRepo.checkExist(op.getCategoryCode(), true);
        categoryRepo.update(op, op.getCategoryCode());
    }

    private void checkParentCode(String parentCode) {
        if(StringUtils.isEmpty(parentCode)) {
            return;
        }
        //加锁，防止执行过程中，变成叶子节点
        OpenapiCategoryDB existed = categoryRepo.queryByUniqueKeyForUpdate(parentCode);
        Assert.notNull(existed, "父类目编码不存在");
        long endpointNum = categoryRepo.countEndpointCategoriesByCategoryCode(parentCode);
        Assert.isTrue(endpointNum == 0, "关联能力点的类目不可作为父类目");
    }

    @Transactional
    public void changeStatus(String categoryCode, boolean active) {
        categoryRepo.checkExist(categoryCode, true);
        String status = active ? ACTIVE : INACTIVE;
        categoryRepo.updateStatus(categoryCode, status);
    }

    public List<OpenapiCategoryDB> listByCondition(Condition.CategoryCondition condition) {
        return categoryRepo.list(condition);
    }

    public Page<OpenapiCategoryDB> pageByCondition(Condition.CategoryCondition condition) {
        return categoryRepo.page(condition);
    }

    @Transactional
    public Boolean addCategoryWithEndpoint(MetaDataOps.EndpointCategoryOp op) {
        OpenapiEndpointCategoryRelationDB existed = categoryRepo.queryByEndpointAndCategoryCode(op.getEndpoint(), op.getEndpoint());
        Assert.isNull(existed, "能力点已存在于该类目下");
        checkLeafCategory(op.getCategoryCode(), false);
        categoryRepo.addRelations(Lists.newArrayList(op));
        return true;
    }

    @Transactional
    public Boolean removeCategoryWithEndpoint(MetaDataOps.EndpointCategoryOp op) {
        OpenapiEndpointCategoryRelationDB existed = categoryRepo.queryByEndpointAndCategoryCode(op.getEndpoint(), op.getEndpoint());
        Assert.notNull(existed, "能力点已不存在于该类目下，无法删除");
        return categoryRepo.deleteRelation(existed.getId()) == 1;
    }

    @Transactional
    public Boolean replaceCategoryWithEndpoint(String endpoint, Set<String> categoryCodes) {
        //当一次性要关联多个节点时，获取其中一个锁失败时不等待，直接抛异常，防止死锁发生
        boolean nowait = categoryCodes.size() > 1;
        categoryCodes.forEach(code -> checkLeafCategory(code, nowait));
        List<OpenapiEndpointCategoryRelationDB> orgin = categoryRepo.listEndpointCategoriesByEndpoint(endpoint);
        List<Long> deletes = new ArrayList<>();
        Set<String> insertCodes = Sets.newHashSet(categoryCodes);
        orgin.forEach(db -> {
            if(categoryCodes.contains(db.getCategoryCode())) {
                insertCodes.remove(db.getCategoryCode());
            } else {
                deletes.add(db.getId());
            }
        });
        List<MetaDataOps.EndpointCategoryOp> inserts = insertCodes.stream()
                .map(code -> MetaDataOps.EndpointCategoryOp.builder().endpoint(endpoint).categoryCode(code).build())
                .collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(deletes)) {
            categoryRepo.deleteRelations(deletes);
        }
        if(!CollectionUtils.isEmpty(inserts)) {
            categoryRepo.addRelations(inserts);
        }
        return true;
    }

    private void checkLeafCategory(String categoryCode, boolean nowait) {
        //加锁，防止执行过程中变成了父节点
        OpenapiCategoryDB existed = nowait ? categoryRepo.queryByUniqueKeyForUpdateNoWait(categoryCode)
                : categoryRepo.queryByUniqueKeyForUpdate(categoryCode);
        Assert.notNull(existed, "类目编码不存在: " + categoryCode);
        long childs = categoryRepo.count(Condition.CategoryCondition.builder().parentCode(categoryCode).build());
        Assert.isTrue(childs == 0, categoryCode + " 已作为父类目，不可关联能力点");
    }

    public EndpointCategoryTree listTree(Condition.CategoryTreeCondition condition) {
        List<OpenapiCategoryDB> categories = categoryRepo.queryAllChildrenIncludeSelfByCategoryCode(condition.getCategoryCode(),
                condition.getStatus());
        OpenapiCategoryDB category = categories.stream().filter(db -> db.getCategoryCode().equals(condition.getCategoryCode())).findAny()
                .orElse(null);
        if(category == null) {
            return null;
        }
        return getTreeByCategoryCode(category,
                categories.stream().collect(Collectors.groupingBy(OpenapiCategoryDB::getParentCode)),
                condition.isIncludeEndpoint());
    }

    private EndpointCategoryTree getTreeByCategoryCode(OpenapiCategoryDB root, Map<String, List<OpenapiCategoryDB>> categories, boolean includeEndpoint) {
        EndpointCategoryTree tree = new EndpointCategoryTree();
        tree.setCategoryCode(root.getCategoryCode());
        tree.setCategoryName(root.getCategoryName());
        List<OpenapiCategoryDB> children = categories.get(root.getCategoryCode());
        if(CollectionUtils.isEmpty(children)) {
            if(includeEndpoint) {
                tree.setEndpoints(listEndpointByCategoryCode(root.getCategoryCode()));
            }
            return tree;
        }
        for (OpenapiCategoryDB child : children) {
            tree.addChild(getTreeByCategoryCode(child, categories, includeEndpoint));
        }
        return tree;
    }

    private List<OpenapiEndpointDB> listEndpointByCategoryCode(String categoryCode) {
        List<OpenapiEndpointCategoryRelationDB> relations = categoryRepo.listEndpointCategoriesByCategoryCodes(Lists.newArrayList(categoryCode));
        Set<String> endpoints = relations.stream().map(OpenapiEndpointCategoryRelationDB::getEndpoint).collect(Collectors.toSet());
        return endpointService.listByCondition(Condition.EndpointCondition.builder()
                .endpoints(endpoints)
                .build());
    }
}
