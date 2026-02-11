package com.kuzmich.schoolbot.core.premium;

/**
 * Сервис проверки доступа к фиче (GATE/QUOTA) и учёта использования квот.
 * Реализация в модуле бота использует SubscriptionService, FeatureConfigService и user_quotas.
 * Фича передаётся через {@link FeatureKey} (в реализации — enum), не строкой.
 */
public interface FeatureAccessService {

    /**
     * Проверяет, может ли пользователь использовать фичу в запрошенном объёме.
     * — Определяет тариф пользователя;
     * — для GATE: разрешено по is_enabled;
     * — для QUOTA: текущее использование + requestedAmount vs limit за период.
     *
     * @param userId          идентификатор пользователя
     * @param feature         фича (enum, реализующий FeatureKey — не произвольная строка)
     * @param requestedAmount запрашиваемое количество (например 1 для одной генерации)
     * @return результат с granted, message и при необходимости remaining
     */
    AccessCheckResult checkAccess(Long userId, FeatureKey feature, int requestedAmount);

    /**
     * Увеличивает счётчик использования квоты для текущего периода (создаёт запись при необходимости).
     */
    void incrementUsage(Long userId, FeatureKey feature, int amount);
}
