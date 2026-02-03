package com.kuzmich.schoolbot.core.bot;

import com.kuzmich.schoolbot.core.handler.callback.CallbackQueryProcessingHandler;
import com.kuzmich.schoolbot.core.handler.command.CommandProcessingHandler;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Базовый класс Telegram-бота (API 9.x) с делегированием обработки команд и callback-запросов.
 * Реализует {@link SpringLongPollingBot} и {@link LongPollingSingleThreadUpdateConsumer};
 * конкретный бот задаёт {@link #getBotToken()} и передаёт в конструктор роутеры команд и callback'ов.
 */
public abstract class AbstractTelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final CommandProcessingHandler commandHandler;
    private final CallbackQueryProcessingHandler callbackHandler;
    private final AtomicReference<TelegramClient> telegramClientRef = new AtomicReference<>();

    protected AbstractTelegramBot(
            CommandProcessingHandler commandHandler,
            CallbackQueryProcessingHandler callbackHandler
    ) {
        this.commandHandler = commandHandler;
        this.callbackHandler = callbackHandler;
    }

    /**
     * Клиент для отправки запросов в API. Создаётся при первом обращении по токену из {@link #getBotToken()}.
     */
    protected TelegramClient getTelegramClient() {
        return telegramClientRef.updateAndGet(
                ref -> ref != null ? ref : new OkHttpTelegramClient(getBotToken())
        );
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        if (update.hasCallbackQuery()) {
            callbackHandler.process(getTelegramClient(), update);
            return;
        }
        if (update.hasMessage() && update.getMessage().hasText()) {
            commandHandler.process(getTelegramClient(), update);
            return;
        }
        onOtherUpdate(update);
    }

    /**
     * Переопределите для обработки прочих типов обновлений (медиа без текста и т.д.).
     * По умолчанию — ничего не делаем.
     */
    protected void onOtherUpdate(Update update) {
        // Подкласс может отправить подсказку о необходимости текстового ввода и т.д.
    }
}
