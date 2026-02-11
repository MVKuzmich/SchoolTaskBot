package com.kuzmich.schoolbot.subscription.mapper;

import com.kuzmich.schoolbot.core.premium.FeatureConfig;
import com.kuzmich.schoolbot.core.premium.QuotaPeriod;
import com.kuzmich.schoolbot.subscription.entity.FeatureConfigEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Маппер между {@link FeatureConfigEntity} и {@link FeatureConfig}.
 * Генерируется MapStruct; defaultAllow — дефолтная конфигурация при отсутствии записи в БД.
 */
@Mapper(componentModel = "spring")
public interface FeatureConfigMapper {

    @Mapping(target = "gate", expression = "java(e.getFeatureType() == com.kuzmich.schoolbot.subscription.entity.FeatureType.GATE)")
    @Mapping(target = "quota", expression = "java(e.getFeatureType() == com.kuzmich.schoolbot.subscription.entity.FeatureType.QUOTA)")
    @Mapping(target = "quotaPeriod", expression = "java(e.getQuotaPeriod() != null ? com.kuzmich.schoolbot.core.premium.QuotaPeriod.valueOf(e.getQuotaPeriod()) : null)")
    @Mapping(target = "isEnabled", expression = "java(java.lang.Boolean.TRUE.equals(e.getIsEnabled()))")
    FeatureConfig toDto(FeatureConfigEntity e);

    /**
     * Дефолтная конфигурация при отсутствии записи в БД (MVP: всё разрешено с щедрым лимитом).
     */
    default FeatureConfig defaultAllow(String featureKey, String tier) {
        return FeatureConfig.builder()
                .featureKey(featureKey)
                .tier(tier)
                .gate(false)
                .quota(true)
                .quotaLimit(null)
                .quotaPeriod(QuotaPeriod.DAY)
                .isEnabled(true)
                .build();
    }
}
