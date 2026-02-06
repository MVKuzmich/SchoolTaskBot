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
        if (update == null || update.getCallbackQuery() == null) {
            log.debug("Получено обновление без callbackQuery: update={}", update);
            return;
        }

        var callbackQuery = update.getCallbackQuery();
        var from = callbackQuery.getFrom();
        var message = callbackQuery.getMessage();

        Long userId = from != null ? from.getId() : null;
        Long chatId = message != null ? message.getChatId() : null;
        String data = callbackQuery.getData();

        for (CallbackQueryHandler handler : handlers) {
            if (handler.canHandle(update)) {
                try {
                    log.info(
                            "callback userId={} chatId={} data={}",
                            userId,
                            chatId,
                            data
                    );
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
