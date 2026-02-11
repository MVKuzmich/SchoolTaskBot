package com.kuzmich.schoolbot.handler;

import com.kuzmich.schoolbot.core.handler.command.CommandHandler;
import com.kuzmich.schoolbot.core.i18n.StartMessageKeys;
import com.kuzmich.schoolbot.core.privacy.ConsentGate;
import com.kuzmich.schoolbot.core.service.MessageService;
import com.kuzmich.schoolbot.core.service.UserContextService;
import com.kuzmich.schoolbot.core.service.UserStateService;
import com.kuzmich.schoolbot.core.validation.Validation;
import com.kuzmich.schoolbot.context.UserContext;
import com.kuzmich.schoolbot.state.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Обработчик команды /start: проверка согласия на обработку ПД, при отсутствии — экран согласия;
 * при наличии — приветствие, установка состояния AWAITING_MODE и клавиатура выбора режима (MathBot-Scenarios).
 */
@Component
@RequiredArgsConstructor
public class StartCommandHandler implements CommandHandler {

    private static final String COMMAND_START = "/start";
    private static final String KEY_CONSENT_REQUEST = "privacy.consent.request";
    private static final String KEY_CONSENT_BUTTON_ACCEPT = "privacy.consent.button.accept";
    private static final String KEY_CONSENT_BUTTON_POLICY = "privacy.consent.button.policy";

    private final ConsentGate consentGate;
    private final MessageService messageService;
    private final UserStateService userStateService;
    private final UserContextService<UserContext> userContextService;

    @Override
    public boolean canHandle(Update update) {
        if (update.getMessage() == null || update.getMessage().getText() == null) {
            return false;
        }
        return update.getMessage().getText().trim().startsWith(COMMAND_START);
    }

    @Override
    public void handle(TelegramClient client, Update update) {
        Validation.requireStartsWith(update.getMessage().getText(), COMMAND_START, "command");
        Long chatId = Validation.requireNonNull(update.getMessage().getChatId(), "chatId");
        Long userId = update.getMessage().getFrom() != null
                ? update.getMessage().getFrom().getId()
                : null;
        if (userId == null) {
            messageService.sendFromKey(client, chatId, StartMessageKeys.START_MESSAGE);
            return;
        }
        if (consentGate.checkAndSendIfNeeded(client, userId, chatId,
                KEY_CONSENT_REQUEST, KEY_CONSENT_BUTTON_ACCEPT, KEY_CONSENT_BUTTON_POLICY)) {
            userStateService.setState(userId, UserState.AWAITING_CONSENT);
            return;
        }
        userStateService.setState(userId, UserState.AWAITING_MODE);
        userContextService.getOrCreate(userId);
        messageService.sendFromKey(client, chatId, StartMessageKeys.START_MESSAGE,
                GeneratorKeyboardFactory.modeSelectionKeyboard());
    }
}
