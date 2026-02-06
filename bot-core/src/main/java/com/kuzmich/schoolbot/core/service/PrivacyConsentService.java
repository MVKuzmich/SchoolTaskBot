package com.kuzmich.schoolbot.core.service;

/**
 * Сервис работы с согласием пользователя на обработку персональных данных.
 * Реализация в конкретном боте (работа с БД, конфигурация url/версии).
 */
public interface PrivacyConsentService {

    /**
     * Есть ли у пользователя действующее согласие для текущей версии политики.
     */
    boolean hasValidConsent(Long userId);

    /**
     * Зафиксировать согласие пользователя (текущее время и текущая версия политики).
     */
    void recordConsent(Long userId);

    /**
     * URL политики конфиденциальности (из конфигурации бота).
     */
    String getPrivacyPolicyUrl();

    /**
     * Текущая версия политики конфиденциальности (из конфигурации бота).
     */
    String getPrivacyPolicyVersion();
}
