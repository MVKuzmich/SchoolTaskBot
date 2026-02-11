package com.kuzmich.schoolbot.context;

import com.kuzmich.schoolbot.core.validation.Validation;
import com.kuzmich.schoolbot.domain.Mode;
import com.kuzmich.schoolbot.domain.SchoolLevel;
import com.kuzmich.schoolbot.domain.Subject;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Сущность JPA для хранения контекста пользователя в PostgreSQL.
 * Одна запись на пользователя (user_id — первичный ключ).
 */
@Entity
@Table(name = "user_context")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserContextEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", length = 20)
    private Mode mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "school_level", length = 20)
    private SchoolLevel schoolLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "subject", length = 20)
    private Subject subject;

    @Column(name = "topic", length = 50)
    private String topic;

    @Column(name = "operation_type", length = 30)
    private String operationType;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "language", length = 10)
    private String language;

    @Column(name = "last_generation_params", columnDefinition = "TEXT")
    private String lastGenerationParams;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    @Column(name = "privacy_consent_at")
    private LocalDateTime privacyConsentAt;

    @Column(name = "privacy_policy_version", length = 50)
    private String privacyPolicyVersion;

    public UserContextEntity(Long userId) {
        this.userId = Validation.requireNonNull(userId, "userId");
        this.registeredAt = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
    }
}
