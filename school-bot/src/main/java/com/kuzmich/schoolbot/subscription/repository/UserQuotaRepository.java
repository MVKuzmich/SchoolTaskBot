package com.kuzmich.schoolbot.subscription.repository;

import com.kuzmich.schoolbot.subscription.entity.UserQuotaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Репозиторий использования квот по фичам и периодам.
 */
public interface UserQuotaRepository extends JpaRepository<UserQuotaEntity, Long> {

    /**
     * Находит запись квоты для пользователя, фичи и начала периода.
     */
    Optional<UserQuotaEntity> findByUserIdAndFeatureKeyAndPeriodStart(
            Long userId, String featureKey, LocalDateTime periodStart);

    /**
     * Суммарное использование за период (если период задаётся одним period_start).
     */
    @Query("SELECT COALESCE(SUM(uq.usageCount), 0) FROM UserQuotaEntity uq " +
           "WHERE uq.userId = :userId AND uq.featureKey = :featureKey AND uq.periodStart = :periodStart")
    int sumUsageForUserAndFeatureAndPeriod(
            @Param("userId") Long userId,
            @Param("featureKey") String featureKey,
            @Param("periodStart") LocalDateTime periodStart);

    /**
     * Атомарно увеличивает использование квоты: вставка строки при отсутствии или инкремент при наличии.
     * Устраняет гонку при параллельных запросах (один SQL вместо read-modify-write).
     */
    @Modifying
    @Query(value = """
            INSERT INTO user_quotas (user_id, feature_key, period_start, period_end, usage_count, created_at)
            VALUES (:userId, :featureKey, :periodStart, :periodEnd, :amount, CURRENT_TIMESTAMP)
            ON CONFLICT (user_id, feature_key, period_start)
            DO UPDATE SET usage_count = user_quotas.usage_count + EXCLUDED.usage_count
            """, nativeQuery = true)
    void incrementUsageAtomic(
            @Param("userId") Long userId,
            @Param("featureKey") String featureKey,
            @Param("periodStart") LocalDateTime periodStart,
            @Param("periodEnd") LocalDateTime periodEnd,
            @Param("amount") int amount);
}
