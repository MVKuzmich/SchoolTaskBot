package com.kuzmich.schoolbot.handler;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

/**
 * Создаёт inline-клавиатуры для сценария генератора (выбор режима, класса, предмета).
 * Тексты кнопок и callback_data соответствуют MathBot-Scenarios.
 */
public final class GeneratorKeyboardFactory {

    private GeneratorKeyboardFactory() {
    }

    /**
     * Клавиатура после /start: выбор режима (Генератор / Тренажёр) и Справка.
     */
    public static InlineKeyboardMarkup modeSelectionKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text("📝 Генератор заданий")
                                .callbackData(CallbackData.MODE_GENERATOR)
                                .build()))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text("🎯 Тренажёр")
                                .callbackData(CallbackData.MODE_TRAINER)
                                .build()))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text("ℹ️ Справка")
                                .callbackData("help")
                                .build()))
                .build();
    }

    /**
     * Клавиатура выбора класса: Начальная / Средняя школа, Назад, Справка.
     */
    public static InlineKeyboardMarkup classSelectionKeyboard(String backLabel, String helpLabel) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text("👧 Начальная школа (1-4 класс)")
                                .callbackData(CallbackData.GEN_ELEMENTARY)
                                .build()))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text("👦 Средняя школа (5-9 класс)")
                                .callbackData(CallbackData.GEN_SECONDARY)
                                .build()))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(backLabel)
                                .callbackData(CallbackData.BACK_TO_MODE)
                                .build(),
                        InlineKeyboardButton.builder()
                                .text(helpLabel)
                                .callbackData("help")
                                .build()))
                .build();
    }

    /**
     * Клавиатура выбора предмета (MVP: только Математика), Назад, Главное меню.
     */
    public static InlineKeyboardMarkup subjectSelectionKeyboard(String mathLabel, String backLabel, String menuLabel) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(mathLabel)
                                .callbackData(CallbackData.SUBJECT_MATH)
                                .build()))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(backLabel)
                                .callbackData(CallbackData.BACK_TO_CLASS)
                                .build(),
                        InlineKeyboardButton.builder()
                                .text(menuLabel)
                                .callbackData(CallbackData.MENU)
                                .build()))
                .build();
    }

    /**
     * Одна кнопка «Главное меню» (например, после заглушки тренажёра).
     */
    public static InlineKeyboardMarkup mainMenuOnlyKeyboard(String menuLabel) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(menuLabel)
                                .callbackData(CallbackData.MENU)
                                .build()))
                .build();
    }
}
