package com.kuzmich.schoolbot.subscription.repository;

import com.kuzmich.schoolbot.subscription.entity.SubscriptionEntity;
import com.kuzmich.schoolbot.subscription.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Репозиторий подписок. Поиск активной подписки по user_id для определения тарифа.
 */
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {

    /**
     * Находит активную подписку пользователя (status = ACTIVE).
     * Для определения тарифа дополнительно проверяют end_date в SubscriptionService.
     */
    Optional<SubscriptionEntity> findByUserIdAndStatus(Long userId, SubscriptionStatus status);
}
