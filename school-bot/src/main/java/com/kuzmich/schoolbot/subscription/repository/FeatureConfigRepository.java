package com.kuzmich.schoolbot.subscription.repository;

import com.kuzmich.schoolbot.subscription.entity.FeatureConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий конфигурации фич по тарифам.
 */
public interface FeatureConfigRepository extends JpaRepository<FeatureConfigEntity, Long> {

    Optional<FeatureConfigEntity> findByFeatureKeyAndTier(String featureKey, String tier);

    List<FeatureConfigEntity> findAllByOrderByFeatureKeyAscTierAsc();
}
