package com.kuzmich.schoolbot.handler;

import com.kuzmich.schoolbot.core.handler.command.CommandHandler;
import com.kuzmich.schoolbot.core.i18n.StartMessageKeys;
import com.kuzmich.schoolbot.core.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Обработчик команды /start — приветствие и краткая инструкция.
 * Текст и отправка — через {@link MessageService} по ключу {@link StartMessageKeys#START_MESSAGE}.
 */
@Component
@RequiredArgsConstructor
public class StartCommandHandler implements CommandHandler {

    private static final String COMMAND_START = "/start";

    private final MessageService messageService;

    @Override
    public boolean canHandle(Update update) {
        String text = update.getMessage().getText();
        return text != null && text.trim().startsWith(COMMAND_START);
    }

    @Override
    public void handle(TelegramClient client, Update update) {
        Long chatId = update.getMessage().getChatId();
        messageService.sendFromKey(client, chatId, StartMessageKeys.START_MESSAGE);
    }
}
