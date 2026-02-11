package com.kuzmich.schoolbot.subscription.service;

import com.kuzmich.schoolbot.core.premium.AccessCheckResult;
import com.kuzmich.schoolbot.core.premium.FeatureConfig;
import com.kuzmich.schoolbot.core.premium.FeatureConfigService;
import com.kuzmich.schoolbot.core.premium.QuotaPeriod;
import com.kuzmich.schoolbot.core.premium.SubscriptionService;
import com.kuzmich.schoolbot.core.premium.SubscriptionTier;
import com.kuzmich.schoolbot.core.service.MessageService;
import com.kuzmich.schoolbot.i18n.FeatureAccessMessageKeys;
import com.kuzmich.schoolbot.subscription.Feature;
import com.kuzmich.schoolbot.subscription.repository.UserQuotaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты FeatureAccessServiceImpl: GATE вкл/выкл, QUOTA в пределах лимита и превышение, без лимита.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class FeatureAccessServiceTest {

    private static final Long USER_ID = 456L;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private FeatureConfigService featureConfigService;

    @Mock
    private UserQuotaRepository userQuotaRepository;

    @Mock
    private MessageService messageService;

    private FeatureAccessServiceImpl featureAccessService;

    @BeforeEach
    void setUp() {
        featureAccessService = new FeatureAccessServiceImpl(
                subscriptionService, featureConfigService, userQuotaRepository, messageService);
    }

    @Test
    @DisplayName("checkAccess GATE: при is_enabled=false возвращает отказ с сообщением про Premium")
    void shouldDenyTrainer_whenGateDisabled_forFreeUser() {
        when(messageService.getText(FeatureAccessMessageKeys.GATE_DISABLED))
                .thenReturn("Эта функция доступна по подписке Premium. Нажмите «💎 Premium» для перехода.");
        when(subscriptionService.getUserTier(USER_ID)).thenReturn(SubscriptionTier.FREE);
        when(featureConfigService.getConfig("TRAINER", "FREE"))
                .thenReturn(FeatureConfig.gate(false));

        AccessCheckResult result = featureAccessService.checkAccess(USER_ID, Feature.TRAINER, 1);

        assertThat(result.isGranted()).isFalse();
        assertThat(result.getMessage()).contains("Premium");
    }

    @Test
    @DisplayName("checkAccess GATE: при is_enabled=true разрешено")
    void shouldGrant_whenGateEnabled() {
        when(subscriptionService.getUserTier(USER_ID)).thenReturn(SubscriptionTier.FREE);
        when(featureConfigService.getConfig("TRAINER", "FREE"))
                .thenReturn(FeatureConfig.gate(true));

        AccessCheckResult result = featureAccessService.checkAccess(USER_ID, Feature.TRAINER, 1);

        assertThat(result.isGranted()).isTrue();
    }

    @Test
    @DisplayName("checkAccess QUOTA: при quota_limit=null (Premium) разрешено")
    void shouldGrantPdfGeneration_forPremiumUser_unlimitedQuota() {
        when(subscriptionService.getUserTier(USER_ID)).thenReturn(SubscriptionTier.PREMIUM);
        when(featureConfigService.getConfig(Feature.PDF_GENERATION.getKey(), "PREMIUM"))
                .thenReturn(FeatureConfig.quota(null, QuotaPeriod.DAY));

        AccessCheckResult result = featureAccessService.checkAccess(USER_ID, Feature.PDF_GENERATION, 1);

        assertThat(result.isGranted()).isTrue();
    }

    @Test
    @DisplayName("checkAccess QUOTA: при лимите 0 и запросе 1 — отказ с сообщением про лимит")
    void shouldDenyPdfGeneration_whenQuotaExceeded_forFreeUser() {
        when(messageService.getText(FeatureAccessMessageKeys.QUOTA_EXCEEDED))
                .thenReturn("Лимит исчерпан. Попробуйте завтра или оформите Premium.");
        when(subscriptionService.getUserTier(USER_ID)).thenReturn(SubscriptionTier.FREE);
        when(featureConfigService.getConfig(Feature.PDF_GENERATION.getKey(), "FREE"))
                .thenReturn(FeatureConfig.quota(0, QuotaPeriod.DAY));

        AccessCheckResult result = featureAccessService.checkAccess(USER_ID, Feature.PDF_GENERATION, 1);

        assertThat(result.isGranted()).isFalse();
        assertThat(result.getMessage()).contains("Лимит");
    }

    @Test
    @DisplayName("incrementUsage: при QUOTA без лимита не падает")
    void incrementUsage_whenUnlimited_doesNotThrow() {
        when(subscriptionService.getUserTier(USER_ID)).thenReturn(SubscriptionTier.PREMIUM);
        when(featureConfigService.getConfig(Feature.PDF_GENERATION.getKey(), "PREMIUM"))
                .thenReturn(FeatureConfig.quota(null, QuotaPeriod.DAY));

        featureAccessService.incrementUsage(USER_ID, Feature.PDF_GENERATION, 1);

        // При quota_limit=null impl выходит раньше и не обращается к userQuotaRepository
        verify(featureConfigService).getConfig(Feature.PDF_GENERATION.getKey(), "PREMIUM");
    }

    @Test
    @DisplayName("incrementUsage: при QUOTA с лимитом вызывает атомарный инкремент в репозитории")
    void incrementUsage_withQuota_callsIncrementUsageAtomic() {
        when(subscriptionService.getUserTier(USER_ID)).thenReturn(SubscriptionTier.PREMIUM);
        when(featureConfigService.getConfig(Feature.PDF_GENERATION.getKey(), "PREMIUM"))
                .thenReturn(FeatureConfig.quota(10, QuotaPeriod.DAY));

        featureAccessService.incrementUsage(USER_ID, Feature.PDF_GENERATION, 1);

        verify(userQuotaRepository).incrementUsageAtomic(
                eq(USER_ID),
                eq(Feature.PDF_GENERATION.getKey()),
                any(java.time.LocalDateTime.class),
                any(java.time.LocalDateTime.class),
                eq(1));
    }

    @Test
    @DisplayName("checkAccess QUOTA: при периоде WEEK учитывает использование и возвращает remaining")
    void checkAccess_quotaWithWeekPeriod_returnsRemaining() {
        when(subscriptionService.getUserTier(USER_ID)).thenReturn(SubscriptionTier.FREE);
        when(featureConfigService.getConfig(Feature.PDF_GENERATION.getKey(), "FREE"))
                .thenReturn(FeatureConfig.quota(5, QuotaPeriod.WEEK));
        when(userQuotaRepository.findByUserIdAndFeatureKeyAndPeriodStart(
                eq(USER_ID), eq(Feature.PDF_GENERATION.getKey()), any(java.time.LocalDateTime.class)))
                .thenReturn(java.util.Optional.empty());

        AccessCheckResult result = featureAccessService.checkAccess(USER_ID, Feature.PDF_GENERATION, 2);

        assertThat(result.isGranted()).isTrue();
        assertThat(result.getRemaining()).isEqualTo(3);
    }

    @Test
    @DisplayName("checkAccess: при userId=null выбрасывает IllegalArgumentException")
    void checkAccess_whenUserIdNull_throws() {
        assertThatThrownBy(() -> featureAccessService.checkAccess(null, Feature.PDF_GENERATION, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId");
    }

    @Test
    @DisplayName("checkAccess: при feature=null выбрасывает IllegalArgumentException")
    void checkAccess_whenFeatureNull_throws() {
        assertThatThrownBy(() -> featureAccessService.checkAccess(USER_ID, null, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("feature");
    }

    @Test
    @DisplayName("checkAccess: при requestedAmount=0 выбрасывает IllegalArgumentException")
    void checkAccess_whenRequestedAmountZero_throws() {
        assertThatThrownBy(() -> featureAccessService.checkAccess(USER_ID, Feature.PDF_GENERATION, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("положительным");
    }

    @Test
    @DisplayName("incrementUsage: при userId=null выбрасывает IllegalArgumentException")
    void incrementUsage_whenUserIdNull_throws() {
        assertThatThrownBy(() -> featureAccessService.incrementUsage(null, Feature.PDF_GENERATION, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId");
    }

    @Test
    @DisplayName("incrementUsage: при amount=0 выбрасывает IllegalArgumentException")
    void incrementUsage_whenAmountZero_throws() {
        assertThatThrownBy(() -> featureAccessService.incrementUsage(USER_ID, Feature.PDF_GENERATION, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("положительным");
    }
}
