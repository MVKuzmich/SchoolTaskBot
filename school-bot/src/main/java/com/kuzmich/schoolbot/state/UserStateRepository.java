package com.kuzmich.schoolbot.state;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Репозиторий состояний пользователей в PostgreSQL.
 */
public interface UserStateRepository extends JpaRepository<UserStateEntity, Long> {

    Optional<UserStateEntity> findByUserId(Long userId);
}
