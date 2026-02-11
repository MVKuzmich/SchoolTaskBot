package com.kuzmich.schoolbot.subscription.service;

import com.kuzmich.schoolbot.core.premium.SubscriptionService;
import com.kuzmich.schoolbot.core.premium.SubscriptionTier;
import com.kuzmich.schoolbot.subscription.entity.SubscriptionEntity;
import com.kuzmich.schoolbot.subscription.entity.SubscriptionStatus;
import com.kuzmich.schoolbot.subscription.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты BotSubscriptionService: getUserTier (FREE при отсутствии/истечении), isPremium, activateSubscription.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class SubscriptionServiceTest {

    private static final Long USER_ID = 123L;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        subscriptionService = new BotSubscriptionService(subscriptionRepository);
    }

    @Test
    @DisplayName("getUserTier: при отсутствии подписки возвращает FREE")
    void getUserTier_whenNoSubscription_returnsFree() {
        when(subscriptionRepository.findByUserIdAndStatus(USER_ID, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        SubscriptionTier tier = subscriptionService.getUserTier(USER_ID);

        assertThat(tier).isEqualTo(SubscriptionTier.FREE);
    }

    @Test
    @DisplayName("getUserTier: при истекшей подписке возвращает FREE")
    void getUserTier_whenExpired_returnsFree() {
        SubscriptionEntity sub = new SubscriptionEntity();
        sub.setTier(SubscriptionTier.PREMIUM);
        sub.setEndDate(LocalDateTime.now().minusDays(1));
        when(subscriptionRepository.findByUserIdAndStatus(USER_ID, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(sub));

        SubscriptionTier tier = subscriptionService.getUserTier(USER_ID);

        assertThat(tier).isEqualTo(SubscriptionTier.FREE);
    }

    @Test
    @DisplayName("getUserTier: при активной подписке возвращает PREMIUM")
    void getUserTier_whenActive_returnsPremium() {
        SubscriptionEntity sub = new SubscriptionEntity();
        sub.setTier(SubscriptionTier.PREMIUM);
        sub.setEndDate(LocalDateTime.now().plusDays(30));
        when(subscriptionRepository.findByUserIdAndStatus(USER_ID, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(sub));

        SubscriptionTier tier = subscriptionService.getUserTier(USER_ID);

        assertThat(tier).isEqualTo(SubscriptionTier.PREMIUM);
    }

    @Test
    @DisplayName("isPremium: true только при активном PREMIUM")
    void isPremium_whenActivePremium_returnsTrue() {
        SubscriptionEntity sub = new SubscriptionEntity();
        sub.setTier(SubscriptionTier.PREMIUM);
        sub.setEndDate(LocalDateTime.now().plusDays(1));
        when(subscriptionRepository.findByUserIdAndStatus(USER_ID, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(sub));

        boolean premium = subscriptionService.isPremium(USER_ID);

        assertThat(premium).isTrue();
    }

    @Test
    @DisplayName("getUserTier: при end_date=null (бессрочная подписка) возвращает PREMIUM")
    void getUserTier_whenEndDateNull_returnsPremium() {
        SubscriptionEntity sub = new SubscriptionEntity();
        sub.setTier(SubscriptionTier.PREMIUM);
        sub.setEndDate(null);
        when(subscriptionRepository.findByUserIdAndStatus(USER_ID, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(sub));

        SubscriptionTier tier = subscriptionService.getUserTier(USER_ID);

        assertThat(tier).isEqualTo(SubscriptionTier.PREMIUM);
    }

    @Test
    @DisplayName("getUserTier: при userId=null выбрасывает IllegalArgumentException")
    void getUserTier_whenUserIdNull_throws() {
        assertThatThrownBy(() -> subscriptionService.getUserTier(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId");
    }

    @Test
    @DisplayName("activateSubscription: сохраняет подписку в репозиторий")
    void activateSubscription_savesEntity() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(30);

        subscriptionService.activateSubscription(USER_ID, SubscriptionTier.PREMIUM, start, end);

        verify(subscriptionRepository).save(org.mockito.ArgumentMatchers.argThat(entity ->
                entity.getUserId().equals(USER_ID)
                        && entity.getTier() == SubscriptionTier.PREMIUM
                        && entity.getStatus() == SubscriptionStatus.ACTIVE
                        && entity.getStartDate().equals(start)
                        && entity.getEndDate().equals(end)
        ));
    }

    @Test
    @DisplayName("activateSubscription: при наличии активной подписки обновляет её, а не создаёт вторую")
    void activateSubscription_whenActiveExists_updatesExisting() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(30);
        SubscriptionEntity existing = new SubscriptionEntity();
        existing.setId(1L);
        existing.setUserId(USER_ID);
        existing.setTier(SubscriptionTier.PREMIUM);
        existing.setEndDate(LocalDateTime.now().plusDays(5));
        existing.setStatus(SubscriptionStatus.ACTIVE);
        when(subscriptionRepository.findByUserIdAndStatus(USER_ID, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(existing));
        when(subscriptionRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(inv -> inv.getArgument(0));

        subscriptionService.activateSubscription(USER_ID, SubscriptionTier.PREMIUM, start, end);

        verify(subscriptionRepository).save(org.mockito.ArgumentMatchers.argThat(entity ->
                entity.getId().equals(1L)
                        && entity.getUserId().equals(USER_ID)
                        && entity.getStartDate().equals(start)
                        && entity.getEndDate().equals(end)
        ));
    }

    @Test
    @DisplayName("activateSubscription: при userId=null выбрасывает IllegalArgumentException")
    void activateSubscription_whenUserIdNull_throws() {
        assertThatThrownBy(() -> subscriptionService.activateSubscription(
                null, SubscriptionTier.PREMIUM, LocalDateTime.now(), LocalDateTime.now().plusDays(30)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId");
    }

    @Test
    @DisplayName("activateSubscription: при tier=null выбрасывает IllegalArgumentException")
    void activateSubscription_whenTierNull_throws() {
        assertThatThrownBy(() -> subscriptionService.activateSubscription(
                USER_ID, null, LocalDateTime.now(), LocalDateTime.now().plusDays(30)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tier");
    }

    @Test
    @DisplayName("activateSubscription: при startDate=null выбрасывает IllegalArgumentException")
    void activateSubscription_whenStartDateNull_throws() {
        assertThatThrownBy(() -> subscriptionService.activateSubscription(
                USER_ID, SubscriptionTier.PREMIUM, null, LocalDateTime.now().plusDays(30)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("startDate");
    }
}
