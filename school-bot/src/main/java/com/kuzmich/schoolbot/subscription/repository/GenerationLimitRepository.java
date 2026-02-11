package com.kuzmich.schoolbot.subscription.repository;

import com.kuzmich.schoolbot.subscription.entity.GenerationLimitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Репозиторий учёта генераций по дням (опционально при переходе на user_quotas).
 */
public interface GenerationLimitRepository extends JpaRepository<GenerationLimitEntity, Long> {

    Optional<GenerationLimitEntity> findByUserIdAndDate(Long userId, LocalDate date);
}
