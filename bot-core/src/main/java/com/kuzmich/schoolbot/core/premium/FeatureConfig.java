package com.kuzmich.schoolbot.core.premium;

import lombok.Builder;
import lombok.Value;

/**
 * DTO конфигурации фичи для тарифа (GATE или QUOTA).
 * Используется FeatureConfigService и FeatureAccessService.
 */
@Value
@Builder
public class FeatureConfig {

    String featureKey;
    String tier;
    boolean gate;           // true = GATE
    boolean quota;          // true = QUOTA
    Integer quotaLimit;      // null = без лимита
    QuotaPeriod quotaPeriod;
    Boolean isEnabled;       // для GATE: доступ разрешён или нет

    /**
     * Конфигурация типа GATE: фича включена или выключена для тарифа.
     */
    public static FeatureConfig gate(boolean isEnabled) {
        return FeatureConfig.builder()
                .gate(true)
                .quota(false)
                .isEnabled(isEnabled)
                .quotaLimit(null)
                .quotaPeriod(null)
                .featureKey(null)
                .tier(null)
                .build();
    }

    /**
     * Конфигурация типа QUOTA: лимит за период (null = без лимита).
     */
    public static FeatureConfig quota(Integer quotaLimit, QuotaPeriod quotaPeriod) {
        return FeatureConfig.builder()
                .gate(false)
                .quota(true)
                .quotaLimit(quotaLimit)
                .quotaPeriod(quotaPeriod)
                .isEnabled(true)
                .featureKey(null)
                .tier(null)
                .build();
    }
}
