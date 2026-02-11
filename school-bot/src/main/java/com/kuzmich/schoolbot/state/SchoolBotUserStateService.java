package com.kuzmich.schoolbot.state;

import com.kuzmich.schoolbot.core.service.UserStateService;
import com.kuzmich.schoolbot.core.validation.Validation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Реализация {@link UserStateService} для SchoolBot на основе PostgreSQL.
 * Состояние пользователя хранится в таблице {@code user_state}; при первом обращении — {@link UserState#INITIAL}.
 */
@Service
@RequiredArgsConstructor
public class SchoolBotUserStateService implements UserStateService {

    private final UserStateRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Object getState(Long userId) {
        Validation.requireNonNull(userId, "userId");
        return repository.findByUserId(userId)
                .map(UserStateEntity::getState)
                .orElse(UserState.INITIAL);
    }

    @Override
    @Transactional
    public void setState(Long userId, Object state) {
        Validation.requireNonNull(userId, "userId");
        if (!(state instanceof UserState)) {
            return;
        }
        UserState newState = (UserState) state;
        UserStateEntity entity = repository.findByUserId(userId)
                .orElseGet(() -> new UserStateEntity(userId, newState));
        entity.setState(newState);
        repository.save(entity);
    }

    @Override
    @Transactional
    public void clearState(Long userId) {
        Validation.requireNonNull(userId, "userId");
        repository.findByUserId(userId).ifPresent(entity -> {
            entity.setState(UserState.INITIAL);
            repository.save(entity);
        });
        // Если записи нет — при следующем getState вернётся INITIAL
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isWaitingForInput(Long userId) {
        Validation.requireNonNull(userId, "userId");
        UserState state = repository.findByUserId(userId)
                .map(UserStateEntity::getState)
                .orElse(UserState.INITIAL);
        return state != UserState.INITIAL
                && state != UserState.COMPLETED
                && state != UserState.ERROR
                && state != UserState.GENERATING;
    }
}
