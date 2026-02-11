package com.kuzmich.schoolbot.subscription.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuzmich.schoolbot.subscription.entity.FeatureConfigAuditEntity;
import com.kuzmich.schoolbot.subscription.entity.FeatureConfigEntity;
import com.kuzmich.schoolbot.subscription.repository.FeatureConfigAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Сервис записи аудита изменений конфигурации фич (кто, когда, что изменил).
 * JSON формируется через ObjectMapper для корректного экранирования и расширяемости.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureConfigAuditService {

    private static final String ACTION_UPDATE = "UPDATE";

    private final FeatureConfigAuditRepository featureConfigAuditRepository;
    private final ObjectMapper objectMapper;

    /**
     * Сериализует состояние конфигурации в JSON для хранения в audit (old_value / new_value).
     */
    public String toAuditJson(FeatureConfigEntity e) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("quotaLimit", e.getQuotaLimit());
        map.put("quotaPeriod", e.getQuotaPeriod() != null ? e.getQuotaPeriod() : "");
        map.put("isEnabled", e.getIsEnabled());
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException ex) {
            log.warn("Не удалось сериализовать конфигурацию в JSON, используем fallback: id={}", e.getId(), ex);
            return "{\"quotaLimit\":" + e.getQuotaLimit()
                    + ",\"quotaPeriod\":\"" + (e.getQuotaPeriod() != null ? e.getQuotaPeriod() : "")
                    + "\",\"isEnabled\":" + e.getIsEnabled() + "}";
        }
    }

    /**
     * Записывает факт обновления конфигурации в таблицу аудита.
     */
    public void recordUpdate(Long adminUserId, String featureKey, String tier, String oldValueJson, String newValueJson) {
        FeatureConfigAuditEntity audit = new FeatureConfigAuditEntity();
        audit.setAdminUserId(adminUserId);
        audit.setFeatureKey(featureKey);
        audit.setTier(tier);
        audit.setAction(ACTION_UPDATE);
        audit.setOldValue(oldValueJson);
        audit.setNewValue(newValueJson);
        audit.setChangedAt(LocalDateTime.now());
        featureConfigAuditRepository.save(audit);
    }
}
