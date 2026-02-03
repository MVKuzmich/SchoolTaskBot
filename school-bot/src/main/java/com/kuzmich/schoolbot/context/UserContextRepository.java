package com.kuzmich.schoolbot.context;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Репозиторий контекста пользователей в PostgreSQL.
 */
public interface UserContextRepository extends JpaRepository<UserContextEntity, Long> {

    Optional<UserContextEntity> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
