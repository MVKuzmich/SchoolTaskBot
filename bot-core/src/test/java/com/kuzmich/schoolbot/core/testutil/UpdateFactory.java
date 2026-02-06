package com.kuzmich.schoolbot.core.testutil;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.User;

/**
 * Утилита для создания объектов {@link Update} в тестах без ручной сборки в каждом тесте.
 * Вместо реальных конструкторов Telegram-классов использует Mockito-моки,
 * чтобы не упираться в Lombok-ограничения (@NonNull и т.п.).
 */
public final class UpdateFactory {

    private UpdateFactory() {
    }

    /**
     * Создаёт Update с текстовым сообщением (команда /start или другой текст).
     *
     * @param chatId идентификатор чата (обязателен для обработчиков команд)
     * @param text   текст сообщения (например, "/start")
     * @param userId идентификатор пользователя; может быть null (обработчик должен это обрабатывать)
     */
    public static Update messageUpdate(Long chatId, String text, Long userId) {
        Update update = org.mockito.Mockito.mock(Update.class);
        Message message = org.mockito.Mockito.mock(Message.class);

        org.mockito.Mockito.when(update.getMessage()).thenReturn(message);
        org.mockito.Mockito.when(message.getText()).thenReturn(text);
        org.mockito.Mockito.when(message.getChatId()).thenReturn(chatId);

        if (userId != null) {
            User from = org.mockito.Mockito.mock(User.class);
            org.mockito.Mockito.when(message.getFrom()).thenReturn(from);
            org.mockito.Mockito.when(from.getId()).thenReturn(userId);
        }

        return update;
    }

    /**
     * Создаёт Update с callback от inline-кнопки.
     *
     * @param chatId  идентификатор чата
     * @param userId  идентификатор пользователя
     * @param data    callback_data кнопки (например, "mode_generator")
     * @param queryId идентификатор callback query для answerCallback
     */
    public static Update callbackUpdate(Long chatId, Long userId, String data, String queryId) {
        Update update = org.mockito.Mockito.mock(Update.class);
        CallbackQuery callbackQuery = org.mockito.Mockito.mock(CallbackQuery.class);
        Message message = org.mockito.Mockito.mock(Message.class);
        User from = org.mockito.Mockito.mock(User.class);

        org.mockito.Mockito.when(update.getCallbackQuery()).thenReturn(callbackQuery);
        org.mockito.Mockito.when(callbackQuery.getData()).thenReturn(data);
        org.mockito.Mockito.when(callbackQuery.getId()).thenReturn(queryId);
        org.mockito.Mockito.when(callbackQuery.getFrom()).thenReturn(from);
        org.mockito.Mockito.when(callbackQuery.getMessage()).thenReturn(message);

        org.mockito.Mockito.when(from.getId()).thenReturn(userId);
        org.mockito.Mockito.when(message.getChatId()).thenReturn(chatId);

        return update;
    }
}
