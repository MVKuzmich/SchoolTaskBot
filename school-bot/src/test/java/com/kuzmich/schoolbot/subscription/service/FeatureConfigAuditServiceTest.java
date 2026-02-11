package com.kuzmich.schoolbot.subscription.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuzmich.schoolbot.subscription.entity.FeatureConfigEntity;
import com.kuzmich.schoolbot.subscription.repository.FeatureConfigAuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты FeatureConfigAuditService: toAuditJson (валидный JSON через ObjectMapper), recordUpdate.
 */
@ExtendWith(MockitoExtension.class)
class FeatureConfigAuditServiceTest {

    @Mock
    private FeatureConfigAuditRepository featureConfigAuditRepository;

    private FeatureConfigAuditService featureConfigAuditService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        featureConfigAuditService = new FeatureConfigAuditService(featureConfigAuditRepository, objectMapper);
    }

    @Test
    @DisplayName("toAuditJson: возвращает валидный JSON с quotaLimit, quotaPeriod, isEnabled")
    void toAuditJson_returnsValidJson() throws Exception {
        FeatureConfigEntity entity = new FeatureConfigEntity();
        entity.setQuotaLimit(5);
        entity.setQuotaPeriod("DAY");
        entity.setIsEnabled(true);

        String json = featureConfigAuditService.toAuditJson(entity);

        assertThat(json).isNotBlank();
        var map = objectMapper.readValue(json, new TypeReference<java.util.Map<String, Object>>() {});
        assertThat(map)
                .containsEntry("quotaLimit", 5)
                .containsEntry("quotaPeriod", "DAY")
                .containsEntry("isEnabled", true);
    }

    @Test
    @DisplayName("toAuditJson: корректно экранирует значения (quotaPeriod с кавычками)")
    void toAuditJson_escapesValues() throws Exception {
        FeatureConfigEntity entity = new FeatureConfigEntity();
        entity.setQuotaLimit(null);
        entity.setQuotaPeriod("DAY\"X");
        entity.setIsEnabled(false);

        String json = featureConfigAuditService.toAuditJson(entity);

        assertThat(json).isNotBlank();
        var map = objectMapper.readValue(json, new TypeReference<java.util.Map<String, Object>>() {});
        assertThat(map).containsEntry("quotaPeriod", "DAY\"X");
    }
}
