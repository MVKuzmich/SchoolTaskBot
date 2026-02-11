package com.kuzmich.schoolbot.core.premium;

/**
 * Период квоты: день, неделя, месяц или всего (TOTAL).
 * Совпадает со значениями quota_period в БД.
 */
public enum QuotaPeriod {
    DAY,
    WEEK,
    MONTH,
    TOTAL
}
