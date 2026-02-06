package com.kuzmich.schoolbot.handler;

import com.kuzmich.schoolbot.core.handler.command.CommandHandler;
import com.kuzmich.schoolbot.core.i18n.StartMessageKeys;
import com.kuzmich.schoolbot.core.service.MessageService;
import com.kuzmich.schoolbot.core.service.UserContextService;
import com.kuzmich.schoolbot.core.service.UserStateService;
import com.kuzmich.schoolbot.context.UserContext;
import com.kuzmich.schoolbot.state.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Objects;

/**
 * Обработчик команды /start: приветствие, установка состояния AWAITING_MODE
 * и показ inline-клавиатуры выбора режима (Генератор / Тренажёр) по MathBot-Scenarios.
 */
@Component
@RequiredArgsConstructor
public class StartCommandHandler implements CommandHandler {

    private static final String COMMAND_START = "/start";

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
        Long chatId = Objects.requireNonNull(update.getMessage().getChatId(), "chatId");
        Long userId = update.getMessage().getFrom() != null
                ? update.getMessage().getFrom().getId()
                : null;
        if (userId == null) {
            messageService.sendFromKey(client, chatId, StartMessageKeys.START_MESSAGE);
            return;
        }
        userStateService.setState(userId, UserState.AWAITING_MODE);
        userContextService.getOrCreate(userId);
        messageService.sendFromKey(client, chatId, StartMessageKeys.START_MESSAGE,
                GeneratorKeyboardFactory.modeSelectionKeyboard());
    }
}
