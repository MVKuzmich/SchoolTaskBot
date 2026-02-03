package com.kuzmich.schoolbot.state;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Сущность JPA для хранения состояния пользователя в PostgreSQL.
 * Одна запись на пользователя (user_id — первичный ключ).
 */
@Entity
@Table(name = "user_state")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStateEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 50)
    private UserState state;

    @Column(name = "state_set_at", nullable = false)
    private LocalDateTime stateSetAt;

    public UserStateEntity(Long userId, UserState state) {
        this.userId = Objects.requireNonNull(userId, "userId");
        this.state = Objects.requireNonNull(state, "state");
        this.stateSetAt = LocalDateTime.now();
    }

    public void setState(UserState state) {
        this.state = Objects.requireNonNull(state, "state");
        this.stateSetAt = LocalDateTime.now();
    }
}
