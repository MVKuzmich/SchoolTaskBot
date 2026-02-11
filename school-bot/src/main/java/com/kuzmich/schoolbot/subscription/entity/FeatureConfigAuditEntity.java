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
 * Запись аудита изменения настроек фичи (кто, когда, что изменил).
 */
@Entity
@Table(name = "feature_config_audit")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class FeatureConfigAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_user_id", nullable = false)
    private Long adminUserId;

    @Column(name = "feature_key", nullable = false, length = 50)
    private String featureKey;

    @Column(name = "tier", nullable = false, length = 20)
    private String tier;

    @Column(name = "action", nullable = false, length = 20)
    private String action;

    @Column(name = "old_value", columnDefinition = "JSONB")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "JSONB")
    private String newValue;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;
}
