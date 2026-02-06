package com.kuzmich.schoolbot.core.privacy;

import com.kuzmich.schoolbot.core.handler.callback.CallbackQueryHandler;
import com.kuzmich.schoolbot.core.service.PrivacyConsentService;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Objects;

/**
 * Обработчик нажатия кнопки «Согласен» на экране согласия.
 * Фиксирует согласие, отвечает на callback и вызывает AfterConsentHandler для показа приветствия.
 * Бин создаётся в модуле бота с внедрением реализации AfterConsentHandler.
 */
public class PrivacyConsentCallbackHandler implements CallbackQueryHandler {

    private final PrivacyConsentService privacyConsentService;
    private final AfterConsentHandler afterConsentHandler;

    public PrivacyConsentCallbackHandler(PrivacyConsentService privacyConsentService,
                                        AfterConsentHandler afterConsentHandler) {
        this.privacyConsentService = privacyConsentService;
        this.afterConsentHandler = Objects.requireNonNull(afterConsentHandler, "afterConsentHandler");
    }

    @Override
    public boolean canHandle(Update update) {
        if (update.getCallbackQuery() == null) {
            return false;
        }
        String data = update.getCallbackQuery().getData();
        return PrivacyConsentConstants.PRIVACY_CONSENT_ACCEPT.equals(data);
    }

    @Override
    public void handle(TelegramClient client, Update update) {
        var callbackQuery = update.getCallbackQuery();
        Long userId = callbackQuery.getFrom() != null ? callbackQuery.getFrom().getId() : null;
        Long chatId = callbackQuery.getMessage() != null ? callbackQuery.getMessage().getChatId() : null;
        String callbackQueryId = callbackQuery.getId();

        if (userId == null || chatId == null) {
            answerCallback(client, callbackQueryId);
            return;
        }

        privacyConsentService.recordConsent(userId);
        answerCallback(client, callbackQueryId);
        afterConsentHandler.onConsentRecorded(client, userId, chatId);
    }

    private void answerCallback(TelegramClient client, String callbackQueryId) {
        try {
            client.execute(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQueryId)
                    .build());
        } catch (TelegramApiException e) {
            throw new RuntimeException("Не удалось ответить на callback", e);
        }
    }
}
