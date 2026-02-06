package com.kuzmich.schoolbot.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Locale;

/**
 * Сервис построения и отправки текстовых сообщений пользователю.
 * Резолвит текст по ключу через MessageSource, собирает SendMessage и выполняет отправку.
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageSource messageSource;

    /**
     * Собирает SendMessage с заданным текстом (без отправки).
     */
    public SendMessage buildText(Long chatId, String text) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
    }

    /**
     * Собирает SendMessage: текст берётся по ключу из messages*.properties.
     */
    public SendMessage buildFromKey(Long chatId, String messageKey, Object... args) {
        String text = messageSource.getMessage(messageKey, args, Locale.getDefault());
        return buildText(chatId, text);
    }

    /**
     * Отправляет в чат сообщение с заданным текстом.
     */
    public void sendText(TelegramClient client, Long chatId, String text) {
        try {
            client.execute(buildText(chatId, text));
        } catch (TelegramApiException e) {
            throw new RuntimeException("Не удалось отправить сообщение", e);
        }
    }

    /**
     * Отправляет в чат сообщение по ключу из messages*.properties.
     */
    public void sendFromKey(TelegramClient client, Long chatId, String messageKey, Object... args) {
        try {
            client.execute(buildFromKey(chatId, messageKey, args));
        } catch (TelegramApiException e) {
            throw new RuntimeException("Не удалось отправить сообщение", e);
        }
    }

    /**
     * Собирает SendMessage: текст по ключу и inline-клавиатура.
     */
    public SendMessage buildFromKey(Long chatId, String messageKey, InlineKeyboardMarkup replyMarkup, Object... args) {
        String text = messageSource.getMessage(messageKey, args, Locale.getDefault());
        return SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(replyMarkup)
                .build();
    }

    /**
     * Отправляет в чат сообщение по ключу с inline-клавиатурой.
     */
    public void sendFromKey(TelegramClient client, Long chatId, String messageKey, InlineKeyboardMarkup replyMarkup, Object... args) {
        try {
            client.execute(buildFromKey(chatId, messageKey, replyMarkup, args));
        } catch (TelegramApiException e) {
            throw new RuntimeException("Не удалось отправить сообщение", e);
        }
    }
}
