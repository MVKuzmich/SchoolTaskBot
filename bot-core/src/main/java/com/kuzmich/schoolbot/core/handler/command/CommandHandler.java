package com.kuzmich.schoolbot.core.handler.command;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Обработчик одной команды (или группы команд) в цепочке ответственности.
 * Роутер {@link CommandProcessingHandler} перебирает зарегистрированные обработчики
 * и вызывает первого, для которого {@link #canHandle(Update)} вернёт true.
 */
public interface CommandHandler {

    /**
     * Может ли этот обработчик обработать данное обновление (команду).
     */
    boolean canHandle(Update update);

    /**
     * Обработать обновление. Вызывается только если {@link #canHandle(Update)} вернул true.
     * @param client клиент API для вызова execute() при отправке сообщений
     */
    void handle(TelegramClient client, Update update);
}
