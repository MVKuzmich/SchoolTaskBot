package com.kuzmich.schoolbot.core.privacy;

import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Вызывается после фиксации согласия пользователя (нажатие «Согласен»).
 * Реализация в модуле бота: установка состояния, приветствие, клавиатура выбора режима и т.п.
 */
public interface AfterConsentHandler {

    /**
     * Пользователь дал согласие; показать основной экран (приветствие и меню).
     */
    void onConsentRecorded(TelegramClient client, Long userId, Long chatId);
}
