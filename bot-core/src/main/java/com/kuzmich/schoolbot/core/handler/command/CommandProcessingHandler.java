package com.kuzmich.schoolbot.core.handler.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

/**
 * Роутер команд: перебирает зарегистрированные {@link CommandHandler} и передаёт
 * обновление первому подходящему (Chain of Responsibility).
 * Список обработчиков инжектируется через конструктор (в т.ч. из Spring — все бины типа CommandHandler).
 */
@Slf4j
@RequiredArgsConstructor
public class CommandProcessingHandler {

    private final List<CommandHandler> handlers;

    public void process(TelegramClient client, Update update) {
        String text = update.getMessage().getText();
        for (CommandHandler handler : handlers) {
            if (handler.canHandle(update)) {
                try {
                    handler.handle(client, update);
                } catch (Exception e) {
                    log.error("Ошибка при обработке команды: {}", text, e);
                }
                return;
            }
        }
        log.debug("Команда не обработана: {}", text);
    }
}
