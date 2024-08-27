package com.ke.bella.openapi.db.repo;

import static com.ke.bella.openapi.Tables.*;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ke.bella.openapi.tables.pojos.ApiKeyMonthCostDB;
import com.ke.bella.openapi.tables.records.ApiKeyMonthCostRecord;

@Component
public class ApikeyCostRepo implements BaseRepo {
    @Resource
    private DSLContext db;

    @Transactional
    public void insert(String akCode, String month, BigDecimal amount) {
        ApiKeyMonthCostRecord rec = API_KEY_MONTH_COST.newRecord();
        rec.setAkCode(akCode);
        rec.setMonth(month);
        rec.setAmount(BigDecimal.ZERO);

        db.insertInto(API_KEY_MONTH_COST).set(rec)
                .onDuplicateKeyIgnore()
                .execute();
    }

    @Transactional
    public void increment(String akCode, String month, BigDecimal cost) {
        db.update(API_KEY_MONTH_COST)
                .set(API_KEY_MONTH_COST.AMOUNT, API_KEY_MONTH_COST.AMOUNT.add(cost))
                .where(API_KEY_MONTH_COST.AK_CODE.eq(akCode))
                .and(API_KEY_MONTH_COST.MONTH.eq(month))
                .execute();
    }

    public BigDecimal queryCost(String akCode, String month) {
        return db.select(API_KEY_MONTH_COST.AMOUNT)
                .from(API_KEY_MONTH_COST)
                .where(API_KEY_MONTH_COST.AK_CODE.eq(akCode))
                .and(API_KEY_MONTH_COST.MONTH.eq(month))
                .fetchOneInto(BigDecimal.class);
    }

    public BigDecimal refreshCache(String akCode, String month) {
        return queryCost(akCode, month);
    }

    public List<ApiKeyMonthCostDB> queryByAkCode(String akCode) {
        return db.selectFrom(API_KEY_MONTH_COST)
                .where(API_KEY_MONTH_COST.AK_CODE.eq(akCode))
                .fetchInto(ApiKeyMonthCostDB.class);
    }
}
