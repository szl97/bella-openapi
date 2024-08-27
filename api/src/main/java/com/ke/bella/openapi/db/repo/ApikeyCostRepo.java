package com.ke.bella.openapi.db.repo;

import com.alicp.jetcache.anno.CachePenetrationProtect;
import com.alicp.jetcache.anno.CacheUpdate;
import com.alicp.jetcache.anno.Cached;
import com.ke.bella.openapi.tables.pojos.ApiKeyMonthCostDB;
import com.ke.bella.openapi.tables.records.ApiKeyMonthCostRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

import static com.ke.bella.openapi.Tables.API_KEY_MONTH_COST;

@Component
public class ApikeyCostRepo implements BaseRepo {
    @Resource
    private DSLContext db;
    @Transactional
    public boolean insert(String akCode, String month, BigDecimal amount) {
        ApiKeyMonthCostRecord rec = API_KEY_MONTH_COST.newRecord();
        rec.setAkCode(akCode);
        rec.setMonth(month);
        rec.setAmount(amount);
        return db.insertInto(API_KEY_MONTH_COST).set(rec)
                .onDuplicateKeyIgnore()
                .execute() > 0;
    }

    @Transactional
    public int increment(String akCode, String month, BigDecimal cost) {
        return db.update(API_KEY_MONTH_COST)
                .set(API_KEY_MONTH_COST.AMOUNT, API_KEY_MONTH_COST.AMOUNT.add(cost))
                .where(API_KEY_MONTH_COST.AK_CODE.eq(akCode))
                .and(API_KEY_MONTH_COST.MONTH.eq(month))
                .execute();
    }

    @Cached(name = "apikey:cost:month:",
            key = "#akCode + ':' + #month",
            cacheNullValue = true,
            expire = 31 * 24 * 3600,
            condition = "T(com.ke.bella.openapi.utils.DateTimeUtils).isCurrentMonth(#month)")
    @CachePenetrationProtect(timeout = 5)
    public BigDecimal queryCost(String akCode, String month) {
        return db.select(API_KEY_MONTH_COST.AMOUNT)
                .from(API_KEY_MONTH_COST)
                .where(API_KEY_MONTH_COST.AK_CODE.eq(akCode))
                .and(API_KEY_MONTH_COST.MONTH.eq(month))
                .fetchOneInto(BigDecimal.class);
    }

    @CacheUpdate(name = "apikey:cost:month:", key = "#akCode + ':' + #month", value = "#result")
    public BigDecimal refreshCache(String akCode, String month) {
        return queryCost(akCode, month);
    }

    public List<ApiKeyMonthCostDB> queryByAkCode(String akCode) {
        return db.selectFrom(API_KEY_MONTH_COST)
                .where(API_KEY_MONTH_COST.AK_CODE.eq(akCode))
                .fetchInto(ApiKeyMonthCostDB.class);
    }
}
