package com.kuzmich.schoolbot.core.privacy;

import com.kuzmich.schoolbot.core.service.MessageService;
import com.kuzmich.schoolbot.core.service.PrivacyConsentService;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * «Ворота» согласия: проверяет наличие действующего согласия и при его отсутствии
 * отправляет экран согласия (текст + кнопки «Согласен» и «Политика конфиденциальности»).
 * Тексты задаются ключами сообщений приложения.
 * Бин создаётся в модуле бота (school-bot), где есть реализация PrivacyConsentService.
 */
public class ConsentGate {

    private final PrivacyConsentService privacyConsentService;
    private final MessageService messageService;

    public ConsentGate(PrivacyConsentService privacyConsentService, MessageService messageService) {
        this.privacyConsentService = privacyConsentService;
        this.messageService = messageService;
    }

    /**
     * Проверяет согласие. Если userId == null или согласие уже есть — возвращает false.
     * Если согласия нет — отправляет сообщение по ключу consentMessageKey (с версией политики как arg)
     * и клавиатуру с кнопками «Согласен» и «Политика конфиденциальности», возвращает true.
     *
     * @param consentMessageKey ключ текста запроса согласия (аргумент {0} — версия политики)
     * @param acceptButtonKey   ключ подписи кнопки «Согласен»
     * @param policyButtonKey   ключ подписи кнопки «Политика конфиденциальности»
     * @return true, если отправлен экран согласия (дальнейшую обработку /start прервать)
     */
    public boolean checkAndSendIfNeeded(TelegramClient client, Long userId, Long chatId,
                                        String consentMessageKey, String acceptButtonKey, String policyButtonKey) {
        if (userId == null) {
            return false;
        }
        if (privacyConsentService.hasValidConsent(userId)) {
            return false;
        }
        String version = privacyConsentService.getPrivacyPolicyVersion();
        String url = privacyConsentService.getPrivacyPolicyUrl();
        InlineKeyboardMarkup keyboard = buildConsentKeyboard(acceptButtonKey, policyButtonKey, url);
        messageService.sendFromKey(client, chatId, consentMessageKey, keyboard, version);
        return true;
    }

    private InlineKeyboardMarkup buildConsentKeyboard(String acceptButtonKey, String policyButtonKey, String policyUrl) {
        String acceptLabel = messageService.getText(acceptButtonKey);
        String policyLabel = messageService.getText(policyButtonKey);
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(acceptLabel)
                                .callbackData(PrivacyConsentConstants.PRIVACY_CONSENT_ACCEPT)
                                .build(),
                        InlineKeyboardButton.builder()
                                .text(policyLabel)
                                .url(policyUrl)
                                .build()))
                .build();
    }
}
