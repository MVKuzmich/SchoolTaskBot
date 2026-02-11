package com.kuzmich.schoolbot.core.premium;

/**
 * Маркер фичи для проверки доступа. Модуль реализации обязан определить enum (или тип),
 * реализующий этот интерфейс, и передавать его в FeatureAccessService — произвольные строки не допускаются.
 */
public interface FeatureKey {

    String getKey();
}
