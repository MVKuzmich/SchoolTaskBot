package com.kuzmich.schoolbot.subscription.service;

import com.kuzmich.schoolbot.core.premium.SubscriptionService;
import com.kuzmich.schoolbot.core.premium.SubscriptionTier;
import com.kuzmich.schoolbot.core.validation.Validation;
import com.kuzmich.schoolbot.subscription.entity.SubscriptionEntity;
import com.kuzmich.schoolbot.subscription.entity.SubscriptionStatus;
import com.kuzmich.schoolbot.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Реализация SubscriptionService: определение тарифа по БД и активация подписки.
 * Если подписки нет или она истекла — считаем пользователя FREE.
 * Хранится одна активная запись на пользователя: при новой активации обновляется существующая.
 * end_date = null трактуется как бессрочная подписка (никогда не истекает).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BotSubscriptionService implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    /**
     * Возвращает текущий тариф пользователя.
     * Нет активной подписки, или end_date в прошлом (если задан) → FREE.
     * end_date = null считается бессрочной подпиской и даёт доступ.
     */
    @Override
    public SubscriptionTier getUserTier(Long userId) {
        Validation.requireNonNull(userId, "userId");
        Optional<SubscriptionEntity> active = subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);
        return active
                .filter(sub -> sub.getEndDate() == null || sub.getEndDate().isAfter(LocalDateTime.now()))
                .map(SubscriptionEntity::getTier)
                .orElse(SubscriptionTier.FREE);
    }

    /**
     * Проверка премиум-доступа по тарифу.
     */
    @Override
    public boolean isPremium(Long userId) {
        return getUserTier(userId) == SubscriptionTier.PREMIUM;
    }

    /**
     * Активирует подписку на заданный период (например после оплаты).
     * На пользователя хранится одна активная запись: при наличии ACTIVE — она обновляется, иначе создаётся новая.
     */
    @Override
    public void activateSubscription(Long userId, SubscriptionTier tier, LocalDateTime startDate, LocalDateTime endDate) {
        Validation.requireNonNull(userId, "userId");
        Validation.requireNonNull(tier, "tier");
        Validation.requireNonNull(startDate, "startDate");
        // endDate может быть null — бессрочная подписка
        SubscriptionEntity sub = subscriptionRepository
                .findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .orElseGet(SubscriptionEntity::new);

        sub.setUserId(userId);
        sub.setTier(tier);
        sub.setStartDate(startDate);
        sub.setEndDate(endDate);
        sub.setStatus(SubscriptionStatus.ACTIVE);
        LocalDateTime now = LocalDateTime.now();
        if (sub.getId() == null) {
            sub.setCreatedAt(now);
        }
        sub.setUpdatedAt(now);
        subscriptionRepository.save(sub);
        log.info("Subscription activated: userId={}, tier={}, until {}", userId, tier, endDate);
    }
}
