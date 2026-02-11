package com.kuzmich.schoolbot.subscription.service;

import com.kuzmich.schoolbot.core.premium.FeatureConfig;
import com.kuzmich.schoolbot.core.premium.FeatureConfigService;
import com.kuzmich.schoolbot.core.validation.Validation;
import com.kuzmich.schoolbot.subscription.entity.FeatureConfigEntity;
import com.kuzmich.schoolbot.subscription.mapper.FeatureConfigMapper;
import com.kuzmich.schoolbot.subscription.repository.FeatureConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Реализация FeatureConfigService: чтение/запись из feature_configs с аудитом изменений.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureConfigServiceImpl implements FeatureConfigService {

    private static final long ADMIN_USER_ID_PLACEHOLDER = 0L; // TODO: передавать из контекста админа

    private final FeatureConfigRepository featureConfigRepository;
    private final FeatureConfigMapper featureConfigMapper;
    private final FeatureConfigAuditService featureConfigAuditService;

    @Override
    public FeatureConfig getConfig(String featureKey, String tier) {
        Validation.requireNotBlank(featureKey, "featureKey");
        Validation.requireNotBlank(tier, "tier");
        Optional<FeatureConfigEntity> opt = featureConfigRepository.findByFeatureKeyAndTier(featureKey, tier);
        if (opt.isEmpty()) {
            return featureConfigMapper.defaultAllow(featureKey, tier);
        }
        return featureConfigMapper.toDto(opt.get());
    }

    @Override
    public List<FeatureConfig> getAllConfigs() {
        return featureConfigRepository.findAllByOrderByFeatureKeyAscTierAsc().stream()
                .map(featureConfigMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void updateConfig(Long configId, FeatureConfigUpdate update) {
        Validation.requireNonNull(configId, "configId");
        Validation.requireNonNull(update, "update");
        FeatureConfigEntity entity = featureConfigRepository.findById(configId)
                .orElseThrow(() -> new IllegalArgumentException("Feature config not found: " + configId));

        String oldValueJson = featureConfigAuditService.toAuditJson(entity);
        if (update.quotaLimit() != null) entity.setQuotaLimit(update.quotaLimit());
        if (update.quotaPeriod() != null) entity.setQuotaPeriod(update.quotaPeriod());
        if (update.isEnabled() != null) entity.setIsEnabled(update.isEnabled());
        entity.setUpdatedAt(LocalDateTime.now());
        featureConfigRepository.save(entity);

        featureConfigAuditService.recordUpdate(
                ADMIN_USER_ID_PLACEHOLDER,
                entity.getFeatureKey(),
                entity.getTier(),
                oldValueJson,
                featureConfigAuditService.toAuditJson(entity));
        log.info("Feature config updated: id={}, key={}, tier={}", configId, entity.getFeatureKey(), entity.getTier());
    }
}
