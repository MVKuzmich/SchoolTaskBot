package com.kuzmich.schoolbot.context;

import com.kuzmich.schoolbot.core.service.UserContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Реализация {@link UserContextService} для SchoolBot на основе PostgreSQL.
 * Контекст хранится в таблице {@code user_context}; при первом обращении создаётся запись.
 */
@Service
@RequiredArgsConstructor
public class SchoolBotUserContextService implements UserContextService<UserContext> {

    private final UserContextRepository repository;
    private final UserContextMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public UserContext getOrCreate(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        return repository.findByUserId(userId)
                .map(mapper::toContext)
                .orElseGet(() -> createAndSave(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserContext> get(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return repository.findByUserId(userId).map(mapper::toContext);
    }

    @Override
    @Transactional
    public void save(UserContext context) {
        if (context == null || context.getUserId() == null) {
            return;
        }
        UserContextEntity entity = repository.findByUserId(context.getUserId())
                .orElseGet(() -> new UserContextEntity(context.getUserId()));
        mapper.updateEntity(context, entity);
        repository.save(entity);
    }

    @Override
    @Transactional
    public void clear(Long userId) {
        if (userId != null) {
            repository.deleteByUserId(userId);
        }
    }

    private UserContext createAndSave(Long userId) {
        UserContext context = new UserContext(userId);
        UserContextEntity entity = new UserContextEntity(userId);
        repository.save(entity);
        return context;
    }
}
