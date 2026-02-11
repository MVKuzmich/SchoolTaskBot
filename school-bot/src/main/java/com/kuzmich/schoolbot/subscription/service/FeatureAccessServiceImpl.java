package com.kuzmich.schoolbot.subscription.service;

import com.kuzmich.schoolbot.core.premium.AccessCheckResult;
import com.kuzmich.schoolbot.core.premium.FeatureAccessService;
import com.kuzmich.schoolbot.core.premium.FeatureConfig;
import com.kuzmich.schoolbot.core.premium.FeatureConfigService;
import com.kuzmich.schoolbot.core.premium.FeatureKey;
import com.kuzmich.schoolbot.core.premium.QuotaPeriod;
import com.kuzmich.schoolbot.core.premium.SubscriptionService;
import com.kuzmich.schoolbot.core.premium.SubscriptionTier;
import com.kuzmich.schoolbot.core.service.MessageService;
import com.kuzmich.schoolbot.core.validation.Validation;
import com.kuzmich.schoolbot.i18n.FeatureAccessMessageKeys;
import com.kuzmich.schoolbot.subscription.entity.UserQuotaEntity;
import com.kuzmich.schoolbot.subscription.repository.UserQuotaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Реализация FeatureAccessService: проверка GATE/QUOTA и учёт использования в user_quotas.
 * Инкремент квоты выполняется атомарно (INSERT ... ON CONFLICT DO UPDATE) для устранения гонок.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureAccessServiceImpl implements FeatureAccessService {

    private final SubscriptionService subscriptionService;
    private final FeatureConfigService featureConfigService;
    private final UserQuotaRepository userQuotaRepository;
    private final MessageService messageService;

    @Override
    public AccessCheckResult checkAccess(Long userId, FeatureKey feature, int requestedAmount) {
        Validation.requireNonNull(userId, "userId");
        Validation.requireNonNull(feature, "feature");
        Validation.requirePositiveInt(requestedAmount, "requestedAmount");
        String featureKey = feature.getKey();
        SubscriptionTier tier = subscriptionService.getUserTier(userId);
        String tierStr = tier.name();
        FeatureConfig config = featureConfigService.getConfig(featureKey, tierStr);

        if (config.isGate()) {
            if (Boolean.FALSE.equals(config.getIsEnabled())) {
                String message = messageService.getText(FeatureAccessMessageKeys.GATE_DISABLED);
                return AccessCheckResult.denied(message);
            }
            return AccessCheckResult.allowed(null);
        }

        if (config.isQuota()) {
            if (config.getQuotaLimit() == null) {
                return AccessCheckResult.allowed(null);
            }
            PeriodBounds bounds = periodBounds(config.getQuotaPeriod());
            int current = getOrCreateUsage(userId, featureKey, bounds.start);
            if (current + requestedAmount > config.getQuotaLimit()) {
                String message = messageService.getText(FeatureAccessMessageKeys.QUOTA_EXCEEDED);
                return AccessCheckResult.denied(message);
            }
            int remaining = config.getQuotaLimit() - current - requestedAmount;
            return AccessCheckResult.allowed(remaining);
        }

        return AccessCheckResult.allowed(null);
    }

    @Override
    @Transactional
    public void incrementUsage(Long userId, FeatureKey feature, int amount) {
        Validation.requireNonNull(userId, "userId");
        Validation.requireNonNull(feature, "feature");
        Validation.requirePositiveInt(amount, "amount");
        String featureKey = feature.getKey();
        FeatureConfig config = featureConfigService.getConfig(featureKey, subscriptionService.getUserTier(userId).name());
        if (!config.isQuota() || config.getQuotaLimit() == null) {
            return;
        }
        PeriodBounds bounds = periodBounds(config.getQuotaPeriod());
        userQuotaRepository.incrementUsageAtomic(userId, featureKey, bounds.start, bounds.end, amount);
    }

    private int getOrCreateUsage(Long userId, String featureKey, LocalDateTime periodStart) {
        Optional<UserQuotaEntity> opt = userQuotaRepository.findByUserIdAndFeatureKeyAndPeriodStart(userId, featureKey, periodStart);
        return opt.map(UserQuotaEntity::getUsageCount).orElse(0);
    }

    private PeriodBounds periodBounds(QuotaPeriod period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start;
        LocalDateTime end;
        if (period == null || period == QuotaPeriod.DAY) {
            start = now.toLocalDate().atStartOfDay();
            end = start.plusDays(1);
        } else if (period == QuotaPeriod.WEEK) {
            start = now.toLocalDate().with(java.time.DayOfWeek.MONDAY).atStartOfDay();
            end = start.plusWeeks(1);
        } else if (period == QuotaPeriod.MONTH) {
            start = now.toLocalDate().atStartOfDay().withDayOfMonth(1);
            end = start.plusMonths(1);
        } else {
            // TOTAL: один общий период (например с начала времён)
            start = LocalDateTime.of(2000, 1, 1, 0, 0);
            end = now.plus(1, ChronoUnit.YEARS);
        }
        return new PeriodBounds(start, end);
    }

    private record PeriodBounds(LocalDateTime start, LocalDateTime end) {}
}
