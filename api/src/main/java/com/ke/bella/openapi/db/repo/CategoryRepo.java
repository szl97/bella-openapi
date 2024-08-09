package com.ke.bella.openapi.db.repo;

import com.ke.bella.openapi.dto.Condition;
import com.ke.bella.openapi.dto.MetaDataOps;
import com.ke.bella.openapi.tables.pojos.OpenapiCategoryDB;
import com.ke.bella.openapi.tables.pojos.OpenapiEndpointCategoryRelationDB;
import com.ke.bella.openapi.tables.records.OpenapiCategoryRecord;
import com.ke.bella.openapi.tables.records.OpenapiEndpointCategoryRelationRecord;
import org.jooq.SelectSeekStep1;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

import static com.ke.bella.openapi.Tables.OPENAPI_ENDPOINT_CATEGORY_RELATION;
import static com.ke.bella.openapi.tables.OpenapiCategory.OPENAPI_CATEGORY;

/**
 * Author: Stan Sai Date: 2024/8/1 21:13 description:
 */
@Component
public class CategoryRepo extends StatusRepo<OpenapiCategoryDB, OpenapiCategoryRecord, String> {

    public int addRelations(List<MetaDataOps.EndpointCategoryOp> ops) {
        List<OpenapiEndpointCategoryRelationRecord> records = ops.stream().map(op -> constructRecord(op)).collect(Collectors.toList());
        return batchInsert(db, records);
    }

    public OpenapiEndpointCategoryRelationRecord constructRecord(MetaDataOps.EndpointCategoryOp op) {
        OpenapiEndpointCategoryRelationRecord rec = OPENAPI_ENDPOINT_CATEGORY_RELATION.newRecord();
        rec.setEndpoint(op.getEndpoint());
        rec.setCategoryCode(op.getCategoryCode());
        fillCreatorInfo(rec);
        return rec;
    }

    public int deleteRelation(Long id) {
        return db.deleteFrom(OPENAPI_ENDPOINT_CATEGORY_RELATION)
                .where(OPENAPI_ENDPOINT_CATEGORY_RELATION.ID.eq(id))
                .execute();
    }

    public int deleteRelations(List<Long> ids) {
        return db.deleteFrom(OPENAPI_ENDPOINT_CATEGORY_RELATION)
                .where(OPENAPI_ENDPOINT_CATEGORY_RELATION.ID.in(ids))
                .execute();
    }

    public List<OpenapiCategoryDB> queryAllChildrenIncludeSelfByCategoryCode(String categoryCode, String status) {
        return db.selectFrom(OPENAPI_CATEGORY)
                .where(OPENAPI_CATEGORY.CATEGORY_CODE.like(categoryCode + "%"))
                .and(StringUtils.isEmpty(status) ? DSL.noCondition() : OPENAPI_CATEGORY.STATUS.eq(status))
                .fetchInto(OpenapiCategoryDB.class);
    }

    public long countChildrenByCategoryCode(String categoryCode, String status) {
        return db.selectFrom(OPENAPI_CATEGORY)
                .where(OPENAPI_CATEGORY.CATEGORY_CODE.like(categoryCode + "%"))
                .and(OPENAPI_CATEGORY.CATEGORY_CODE.ne(categoryCode))
                .and(StringUtils.isEmpty(status) ? DSL.noCondition() : OPENAPI_CATEGORY.STATUS.eq(status))
                .stream().count();
    }

    public long countParentByCategoryCode(String categoryCode, String status) {
        return db.selectFrom(OPENAPI_CATEGORY)
                .where(OPENAPI_CATEGORY.CATEGORY_CODE.like("%" + categoryCode))
                .and(OPENAPI_CATEGORY.CATEGORY_CODE.ne(categoryCode))
                .and(StringUtils.isEmpty(status) ? DSL.noCondition() : OPENAPI_CATEGORY.STATUS.eq(status))
                .stream().count();
    }

    public OpenapiEndpointCategoryRelationDB queryByEndpointAndCategoryCode(String endpoint, String categoryCode) {
        return db.selectFrom(OPENAPI_ENDPOINT_CATEGORY_RELATION)
                .where(OPENAPI_ENDPOINT_CATEGORY_RELATION.ENDPOINT.eq(endpoint))
                .and(OPENAPI_ENDPOINT_CATEGORY_RELATION.CATEGORY_CODE.eq(categoryCode))
                .fetchOneInto(OpenapiEndpointCategoryRelationDB.class);
    }

    public List<OpenapiEndpointCategoryRelationDB> listEndpointCategoriesByCategoryCodes(List<String> categoryCodes) {
        return db.selectFrom(OPENAPI_ENDPOINT_CATEGORY_RELATION)
                .where(OPENAPI_ENDPOINT_CATEGORY_RELATION.CATEGORY_CODE.in(categoryCodes))
                .fetchInto(OpenapiEndpointCategoryRelationDB.class);
    }

    public List<OpenapiEndpointCategoryRelationDB> listEndpointCategoriesByEndpoint(String endpoint) {
        return db.selectFrom(OPENAPI_ENDPOINT_CATEGORY_RELATION)
                .where(OPENAPI_ENDPOINT_CATEGORY_RELATION.ENDPOINT.eq(endpoint))
                .fetchInto(OpenapiEndpointCategoryRelationDB.class);
    }

    public long countEndpointCategoriesByCategoryCode(String categoryCode) {
        return db.selectFrom(OPENAPI_ENDPOINT_CATEGORY_RELATION)
                .where(OPENAPI_ENDPOINT_CATEGORY_RELATION.CATEGORY_CODE.eq(categoryCode))
                .stream().count();
    }

    public long count(Condition.CategoryCondition op) {
        return constructSql(op).stream().count();
    }

    public List<OpenapiCategoryDB> list(Condition.CategoryCondition op) {
        return constructSql(op).fetchInto(OpenapiCategoryDB.class);
    }

    public Page<OpenapiCategoryDB> page(Condition.CategoryCondition op) {
        return queryPage(db, constructSql(op), op.getPageNum(), op.getPageSize(), OpenapiCategoryDB.class);
    }

    private SelectSeekStep1<OpenapiCategoryRecord, Long> constructSql(Condition.CategoryCondition op) {
        return db.selectFrom(OPENAPI_CATEGORY)
                .where(StringUtils.isEmpty(op.getCategoryCode()) ? DSL.noCondition() : OPENAPI_CATEGORY.CATEGORY_CODE.eq(op.getCategoryCode()))
                .and(StringUtils.isEmpty(op.getCategoryName())
                        ? DSL.noCondition()
                        : OPENAPI_CATEGORY.CATEGORY_NAME.like("%" + op.getCategoryName() + "%"))
                .and(StringUtils.isEmpty(op.getParentCode()) ? DSL.noCondition() : OPENAPI_CATEGORY.PARENT_CODE.eq(op.getParentCode()))
                .and(Boolean.TRUE.equals(op.getTopCategory()) ? OPENAPI_CATEGORY.PARENT_CODE.eq("") : DSL.noCondition())
                .and(StringUtils.isEmpty(op.getStatus()) ? DSL.noCondition() : OPENAPI_CATEGORY.STATUS.eq(op.getStatus()))
                .orderBy(OPENAPI_CATEGORY.ID.desc());
    }

    @Override
    public TableImpl<OpenapiCategoryRecord> table() {
        return OPENAPI_CATEGORY;
    }

    @Override
    protected TableField<OpenapiCategoryRecord, String> uniqueKey() {
        return OPENAPI_CATEGORY.CATEGORY_CODE;
    }

    @Override
    protected TableField<OpenapiCategoryRecord, String> statusFiled() {
        return OPENAPI_CATEGORY.STATUS;
    }
}
