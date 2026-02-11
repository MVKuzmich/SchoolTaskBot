package com.kuzmich.schoolbot.context;

import com.kuzmich.schoolbot.core.service.PrivacyConsentService;
import com.kuzmich.schoolbot.core.validation.Validation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Реализация {@link PrivacyConsentService} для SchoolBot.
 * Согласие хранится в таблице user_context (поля privacy_consent_at, privacy_policy_version).
 * URL и версия политики задаются в конфигурации (privacy.policy.url, privacy.policy.version).
 */
@Service
@RequiredArgsConstructor
public class SchoolBotPrivacyConsentService implements PrivacyConsentService {

    @Value("${privacy.policy.url:}")
    private String privacyPolicyUrl;

    @Value("${privacy.policy.version:v1.0}")
    private String privacyPolicyVersion;

    private final UserContextRepository repository;

    @Override
    @Transactional(readOnly = true)
    public boolean hasValidConsent(Long userId) {
        Validation.requireNonNull(userId, "userId");
        return repository.findById(userId)
                .filter(e -> e.getPrivacyConsentAt() != null)
                .filter(e -> Objects.equals(e.getPrivacyPolicyVersion(), privacyPolicyVersion))
                .isPresent();
    }

    @Override
    @Transactional
    public void recordConsent(Long userId) {
        Validation.requireNonNull(userId, "userId");
        UserContextEntity entity = repository.findById(userId)
                .orElseGet(() -> {
                    UserContextEntity newEntity = new UserContextEntity(userId);
                    repository.save(newEntity);
                    return newEntity;
                });
        entity.setPrivacyConsentAt(LocalDateTime.now());
        entity.setPrivacyPolicyVersion(privacyPolicyVersion);
        repository.save(entity);
    }

    @Override
    public String getPrivacyPolicyUrl() {
        return privacyPolicyUrl != null ? privacyPolicyUrl : "";
    }

    @Override
    public String getPrivacyPolicyVersion() {
        return privacyPolicyVersion != null ? privacyPolicyVersion : "v1.0";
    }
}
