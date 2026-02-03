package com.kuzmich.schoolbot.core.handler.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

/**
 * Роутер callback-запросов: перебирает зарегистрированные {@link CallbackQueryHandler}
 * и передаёт обновление первому подходящему (Chain of Responsibility).
 */
@Slf4j
@RequiredArgsConstructor
public class CallbackQueryProcessingHandler {

    private final List<CallbackQueryHandler> handlers;

    public void process(TelegramClient client, Update update) {
        String data = update.getCallbackQuery().getData();
        for (CallbackQueryHandler handler : handlers) {
            if (handler.canHandle(update)) {
                try {
                    handler.handle(client, update);
                } catch (Exception e) {
                    log.error("Ошибка при обработке callback: {}", data, e);
                }
                return;
            }
        }
        log.debug("Callback не обработан: {}", data);
    }
}
