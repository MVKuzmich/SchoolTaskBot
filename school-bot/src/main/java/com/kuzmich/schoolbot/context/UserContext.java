package com.kuzmich.schoolbot.context;

import com.kuzmich.schoolbot.domain.Mode;
import com.kuzmich.schoolbot.domain.SchoolLevel;
import com.kuzmich.schoolbot.domain.Subject;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Контекст пользователя — «что выбрал пользователь» в текущей сессии.
 * Используется для формирования меню и запроса генерации; поддерживает «Создать ещё раз».
 */
@Getter
public class UserContext {

    private final Long userId;

    private Mode mode;
    private SchoolLevel schoolLevel;
    private Subject subject;
    private String topic;
    private String operationType;
    private Integer quantity;

    private String language;

    /** JSON с последними параметрами генерации для «Создать ещё раз». */
    private String lastGenerationParams;

    private LocalDateTime lastActivity;
    private LocalDateTime registeredAt;

    public UserContext(Long userId) {
        this.userId = Objects.requireNonNull(userId, "userId");
        this.registeredAt = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        touch();
    }

    public void setSchoolLevel(SchoolLevel schoolLevel) {
        this.schoolLevel = schoolLevel;
        touch();
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
        touch();
    }

    public void setTopic(String topic) {
        this.topic = topic;
        touch();
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
        touch();
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        touch();
    }

    public void setLanguage(String language) {
        this.language = language;
        touch();
    }

    public void setLastGenerationParams(String lastGenerationParams) {
        this.lastGenerationParams = lastGenerationParams;
        touch();
    }

    /** Для восстановления из БД при загрузке контекста (используется маппером). */
    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }

    /** Для восстановления из БД при загрузке контекста (используется маппером). */
    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    private void touch() {
        this.lastActivity = LocalDateTime.now();
    }
}
