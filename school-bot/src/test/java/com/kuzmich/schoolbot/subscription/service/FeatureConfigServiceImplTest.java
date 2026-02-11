package com.kuzmich.schoolbot.subscription.service;

import com.kuzmich.schoolbot.core.premium.FeatureConfig;
import com.kuzmich.schoolbot.core.premium.FeatureConfigService;
import com.kuzmich.schoolbot.core.premium.QuotaPeriod;
import com.kuzmich.schoolbot.subscription.entity.FeatureConfigEntity;
import com.kuzmich.schoolbot.subscription.entity.FeatureType;
import com.kuzmich.schoolbot.subscription.mapper.FeatureConfigMapper;
import com.kuzmich.schoolbot.subscription.repository.FeatureConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты FeatureConfigServiceImpl: getConfig (из БД и дефолт), getAllConfigs, updateConfig с аудитом.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class FeatureConfigServiceImplTest {

    private static final String PDF_GENERATION = "PDF_GENERATION";
    private static final String FREE = "FREE";

    @Mock
    private FeatureConfigRepository featureConfigRepository;

    @Mock
    private FeatureConfigMapper featureConfigMapper;

    @Mock
    private FeatureConfigAuditService featureConfigAuditService;

    private FeatureConfigServiceImpl featureConfigService;

    @BeforeEach
    void setUp() {
        featureConfigService = new FeatureConfigServiceImpl(
                featureConfigRepository, featureConfigMapper, featureConfigAuditService);
    }

    @Test
    @DisplayName("getConfig: при отсутствии записи возвращает щедрый дефолт (разрешено)")
    void getConfig_whenMissing_returnsDefaultAllow() {
        when(featureConfigRepository.findByFeatureKeyAndTier(PDF_GENERATION, FREE)).thenReturn(Optional.empty());
        FeatureConfig defaultConfig = FeatureConfig.builder()
                .featureKey(PDF_GENERATION)
                .tier(FREE)
                .gate(false)
                .quota(true)
                .quotaLimit(null)
                .quotaPeriod(QuotaPeriod.DAY)
                .isEnabled(true)
                .build();
        when(featureConfigMapper.defaultAllow(PDF_GENERATION, FREE)).thenReturn(defaultConfig);

        FeatureConfig config = featureConfigService.getConfig(PDF_GENERATION, FREE);

        assertThat(config).isNotNull();
        assertThat(config.getFeatureKey()).isEqualTo(PDF_GENERATION);
        assertThat(config.getTier()).isEqualTo(FREE);
        assertThat(config.getIsEnabled()).isTrue();
        assertThat(config.getQuotaLimit()).isNull();
    }

    @Test
    @DisplayName("getConfig: при наличии записи возвращает DTO из entity")
    void getConfig_whenPresent_returnsDtoFromEntity() {
        FeatureConfigEntity entity = new FeatureConfigEntity();
        entity.setId(1L);
        entity.setFeatureKey(PDF_GENERATION);
        entity.setTier(FREE);
        entity.setFeatureType(FeatureType.QUOTA);
        entity.setQuotaLimit(5);
        entity.setQuotaPeriod("DAY");
        entity.setIsEnabled(true);
        when(featureConfigRepository.findByFeatureKeyAndTier(PDF_GENERATION, FREE)).thenReturn(Optional.of(entity));
        FeatureConfig dto = FeatureConfig.builder()
                .featureKey(PDF_GENERATION)
                .tier(FREE)
                .gate(false)
                .quota(true)
                .quotaLimit(5)
                .quotaPeriod(QuotaPeriod.DAY)
                .isEnabled(true)
                .build();
        when(featureConfigMapper.toDto(entity)).thenReturn(dto);

        FeatureConfig config = featureConfigService.getConfig(PDF_GENERATION, FREE);

        assertThat(config.getFeatureKey()).isEqualTo(PDF_GENERATION);
        assertThat(config.getTier()).isEqualTo(FREE);
        assertThat(config.isQuota()).isTrue();
        assertThat(config.getQuotaLimit()).isEqualTo(5);
        assertThat(config.getQuotaPeriod().name()).isEqualTo("DAY");
    }

    @Test
    @DisplayName("getAllConfigs: возвращает список из репозитория")
    void getAllConfigs_returnsListFromRepository() {
        FeatureConfigEntity e = new FeatureConfigEntity();
        e.setFeatureKey(PDF_GENERATION);
        e.setTier(FREE);
        e.setFeatureType(FeatureType.QUOTA);
        e.setQuotaLimit(10);
        e.setQuotaPeriod("DAY");
        e.setIsEnabled(true);
        when(featureConfigRepository.findAllByOrderByFeatureKeyAscTierAsc()).thenReturn(List.of(e));
        FeatureConfig dto = FeatureConfig.builder()
                .featureKey(PDF_GENERATION)
                .tier(FREE)
                .gate(false)
                .quota(true)
                .quotaLimit(10)
                .quotaPeriod(QuotaPeriod.DAY)
                .isEnabled(true)
                .build();
        when(featureConfigMapper.toDto(any())).thenReturn(dto);

        List<FeatureConfig> list = featureConfigService.getAllConfigs();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getFeatureKey()).isEqualTo(PDF_GENERATION);
    }

    @Test
    @DisplayName("updateConfig: обновляет entity и пишет запись в аудит")
    void updateConfig_updatesEntityAndAudit() {
        FeatureConfigEntity entity = new FeatureConfigEntity();
        entity.setId(100L);
        entity.setFeatureKey(PDF_GENERATION);
        entity.setTier(FREE);
        entity.setFeatureType(FeatureType.QUOTA);
        entity.setQuotaLimit(5);
        entity.setQuotaPeriod("DAY");
        entity.setIsEnabled(true);
        when(featureConfigRepository.findById(100L)).thenReturn(Optional.of(entity));
        when(featureConfigRepository.save(entity)).thenReturn(entity);
        when(featureConfigAuditService.toAuditJson(any())).thenReturn("{}");

        featureConfigService.updateConfig(100L, new FeatureConfigService.FeatureConfigUpdate(10, "DAY", null));

        assertThat(entity.getQuotaLimit()).isEqualTo(10);
        verify(featureConfigAuditService).recordUpdate(0L, PDF_GENERATION, FREE, "{}", "{}");
    }

    @Test
    @DisplayName("updateConfig: обновляет isEnabled и quotaPeriod")
    void updateConfig_updatesIsEnabledAndQuotaPeriod() {
        FeatureConfigEntity entity = new FeatureConfigEntity();
        entity.setId(2L);
        entity.setFeatureKey("TRAINER");
        entity.setTier(FREE);
        entity.setFeatureType(FeatureType.GATE);
        entity.setIsEnabled(true);
        entity.setQuotaPeriod(null);
        when(featureConfigRepository.findById(2L)).thenReturn(Optional.of(entity));
        when(featureConfigRepository.save(entity)).thenReturn(entity);
        when(featureConfigAuditService.toAuditJson(any())).thenReturn("{}");

        featureConfigService.updateConfig(2L, new FeatureConfigService.FeatureConfigUpdate(null, "WEEK", false));

        assertThat(entity.getIsEnabled()).isFalse();
        assertThat(entity.getQuotaPeriod()).isEqualTo("WEEK");
        verify(featureConfigAuditService).recordUpdate(0L, "TRAINER", FREE, "{}", "{}");
    }

    @Test
    @DisplayName("getConfig: при featureKey=null выбрасывает IllegalArgumentException")
    void getConfig_whenFeatureKeyNull_throws() {
        assertThatThrownBy(() -> featureConfigService.getConfig(null, FREE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("featureKey");
    }

    @Test
    @DisplayName("getConfig: при tier=null выбрасывает IllegalArgumentException")
    void getConfig_whenTierNull_throws() {
        assertThatThrownBy(() -> featureConfigService.getConfig(PDF_GENERATION, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tier");
    }

    @Test
    @DisplayName("updateConfig: при configId=null выбрасывает IllegalArgumentException")
    void updateConfig_whenConfigIdNull_throws() {
        assertThatThrownBy(() -> featureConfigService.updateConfig(null, new FeatureConfigService.FeatureConfigUpdate(5, null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("configId");
    }

    @Test
    @DisplayName("updateConfig: при update=null выбрасывает IllegalArgumentException")
    void updateConfig_whenUpdateNull_throws() {
        assertThatThrownBy(() -> featureConfigService.updateConfig(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("update");
    }
}
