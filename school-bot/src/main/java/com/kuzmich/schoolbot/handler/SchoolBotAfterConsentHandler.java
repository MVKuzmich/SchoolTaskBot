package com.kuzmich.schoolbot.handler;

import com.kuzmich.schoolbot.context.UserContext;
import com.kuzmich.schoolbot.core.i18n.StartMessageKeys;
import com.kuzmich.schoolbot.core.privacy.AfterConsentHandler;
import com.kuzmich.schoolbot.core.service.MessageService;
import com.kuzmich.schoolbot.core.service.UserContextService;
import com.kuzmich.schoolbot.core.service.UserStateService;
import com.kuzmich.schoolbot.state.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * После нажатия «Согласен»: переход в AWAITING_MODE и показ приветствия с клавиатурой выбора режима.
 */
@Component
@RequiredArgsConstructor
public class SchoolBotAfterConsentHandler implements AfterConsentHandler {

    private final UserStateService userStateService;
    private final UserContextService<UserContext> userContextService;
    private final MessageService messageService;

    @Override
    public void onConsentRecorded(TelegramClient client, Long userId, Long chatId) {
        userStateService.setState(userId, UserState.AWAITING_MODE);
        userContextService.getOrCreate(userId);
        messageService.sendFromKey(client, chatId, StartMessageKeys.START_MESSAGE,
                GeneratorKeyboardFactory.modeSelectionKeyboard());
    }
}
