package com.kuzmich.schoolbot.core.premium;

import java.util.List;

/**
 * Сервис конфигурации фич: чтение/запись правил доступа и лимитов по тарифам.
 * Реализация в school-bot читает из БД (feature_configs); при обновлении пишет в feature_config_audit.
 */
public interface FeatureConfigService {

    /**
     * Возвращает конфигурацию фичи для указанного тарифа.
     * tier — значение тарифа (FREE, PREMIUM) в виде строки.
     * При отсутствии конфигурации — дефолт для MVP (всё разрешено) или исключение.
     */
    FeatureConfig getConfig(String featureKey, String tier);

    /**
     * Все настройки фич (для админ-панели).
     */
    List<FeatureConfig> getAllConfigs();

    /**
     * Обновляет настройки и записывает запись в аудит.
     */
    void updateConfig(Long configId, FeatureConfigUpdate update);

    /**
     * DTO для обновления конфигурации (лимит, is_enabled и т.д.).
     */
    record FeatureConfigUpdate(Integer quotaLimit, String quotaPeriod, Boolean isEnabled) {}
}
