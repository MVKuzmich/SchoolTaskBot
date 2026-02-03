package com.kuzmich.schoolbot.core.handler.callback;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Обработчик callback-запросов (нажатия inline-кнопок) в цепочке ответственности.
 * Роутер {@link CallbackQueryProcessingHandler} передаёт обновление первому обработчику,
 * для которого {@link #canHandle(Update)} вернёт true.
 */
public interface CallbackQueryHandler {

    /**
     * Может ли этот обработчик обработать данный callback (по префиксу data, типу и т.д.).
     */
    boolean canHandle(Update update);

    /**
     * Обработать callback. Вызывается только если {@link #canHandle(Update)} вернул true.
     * @param client клиент API для вызова execute() при отправке сообщений
     */
    void handle(TelegramClient client, Update update);
}
