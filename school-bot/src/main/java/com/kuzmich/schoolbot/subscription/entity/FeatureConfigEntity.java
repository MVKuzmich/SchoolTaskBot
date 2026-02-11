package com.kuzmich.schoolbot.subscription.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Конфигурация фичи для тарифа (GATE или QUOTA).
 * Уникальность по паре (feature_key, tier). Управление без деплоя через админку.
 */
@Entity
@Table(name = "feature_configs")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class FeatureConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "feature_key", nullable = false, length = 50)
    private String featureKey;

    @Column(name = "tier", nullable = false, length = 20)
    private String tier;

    @Enumerated(EnumType.STRING)
    @Column(name = "feature_type", nullable = false, length = 10)
    private FeatureType featureType;

    @Column(name = "quota_limit")
    private Integer quotaLimit;

    @Column(name = "quota_period", length = 10)
    private String quotaPeriod;

    @Column(name = "is_enabled")
    private Boolean isEnabled;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
