package com.kuzmich.schoolbot.subscription.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Использование квоты по фиче и пользователю за период (period_start).
 * Уникальность по (user_id, feature_key, period_start).
 */
@Entity
@Table(name = "user_quotas")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class UserQuotaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "feature_key", nullable = false, length = 50)
    private String featureKey;

    @Column(name = "usage_count", nullable = false)
    private int usageCount;

    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;

    @Column(name = "period_end")
    private LocalDateTime periodEnd;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
