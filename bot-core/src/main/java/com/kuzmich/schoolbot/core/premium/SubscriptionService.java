package com.kuzmich.schoolbot.core.premium;

import java.time.LocalDateTime;

/**
 * Сервис подписок: определение тарифа пользователя и активация подписки.
 * Реализация в school-bot использует БД (subscriptions); при отсутствии или истечении подписки — FREE.
 */
public interface SubscriptionService {

    /**
     * Возвращает текущий тариф пользователя.
     * Нет активной подписки или end_date в прошлом → FREE.
     */
    SubscriptionTier getUserTier(Long userId);

    /**
     * Проверка премиум-доступа по тарифу.
     */
    boolean isPremium(Long userId);

    /**
     * Активирует подписку на заданный период (например после оплаты).
     */
    void activateSubscription(Long userId, SubscriptionTier tier, LocalDateTime startDate, LocalDateTime endDate);
}
