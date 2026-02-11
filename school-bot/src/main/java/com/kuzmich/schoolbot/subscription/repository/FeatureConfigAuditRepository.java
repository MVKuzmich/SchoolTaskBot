package com.kuzmich.schoolbot.subscription.repository;

import com.kuzmich.schoolbot.subscription.entity.FeatureConfigAuditEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Репозиторий аудита изменений конфигурации фич.
 */
public interface FeatureConfigAuditRepository extends JpaRepository<FeatureConfigAuditEntity, Long> {

    List<FeatureConfigAuditEntity> findAllByOrderByChangedAtDesc(Pageable pageable);
}
