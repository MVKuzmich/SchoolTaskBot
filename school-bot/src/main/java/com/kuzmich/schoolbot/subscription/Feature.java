package com.kuzmich.schoolbot.subscription;

import com.kuzmich.schoolbot.core.premium.FeatureKey;
import lombok.RequiredArgsConstructor;

/**
 * Перечень фич приложения; ключи совпадают с feature_key в БД.
 * Используется при вызове FeatureAccessService.checkAccess / incrementUsage.
 * Находится в модуле реализации (school-bot), т.к. набор фич зависит от конкретного бота.
 */
@RequiredArgsConstructor
public enum Feature implements FeatureKey {

    PDF_GENERATION("PDF_GENERATION"),
    EXAMPLE_COUNT("EXAMPLE_COUNT"),
    TRAINER("TRAINER");

    private final String key;

    @Override
    public String getKey() {
        return key;
    }
}
