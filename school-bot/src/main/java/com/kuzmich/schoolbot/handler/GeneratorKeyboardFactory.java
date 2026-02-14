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
     * Клавиатура выбора темы (пока одна — Арифметика).
     */
    public static InlineKeyboardMarkup topicSelectionKeyboard(String arithmeticLabel, String backLabel, String menuLabel) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(arithmeticLabel)
                                .callbackData(CallbackData.TOPIC_ARITHMETIC)
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
     * Клавиатура выбора типа операции арифметики.
     */
    public static InlineKeyboardMarkup operationSelectionKeyboard(String add10Label,
                                                                  String sub10Label,
                                                                  String add20NoCarryLabel,
                                                                  String sub20NoCarryLabel,
                                                                  String backLabel,
                                                                  String menuLabel) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(add10Label)
                                .callbackData(CallbackData.OP_ADDITION_10)
                                .build()))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(sub10Label)
                                .callbackData(CallbackData.OP_SUBTRACTION_10)
                                .build()))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(add20NoCarryLabel)
                                .callbackData(CallbackData.OP_ADDITION_20_NO_CARRY)
                                .build()))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(sub20NoCarryLabel)
                                .callbackData(CallbackData.OP_SUBTRACTION_20_NO_CARRY)
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
     * Клавиатура выбора количества примеров (пресеты 10, 20, 30, 50).
     */
    public static InlineKeyboardMarkup quantitySelectionKeyboard(String qty10Label,
                                                                 String qty20Label,
                                                                 String qty30Label,
                                                                 String qty50Label,
                                                                 String backLabel,
                                                                 String menuLabel) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(qty10Label)
                                .callbackData(CallbackData.QTY_10)
                                .build(),
                        InlineKeyboardButton.builder()
                                .text(qty20Label)
                                .callbackData(CallbackData.QTY_20)
                                .build()))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(qty30Label)
                                .callbackData(CallbackData.QTY_30)
                                .build(),
                        InlineKeyboardButton.builder()
                                .text(qty50Label)
                                .callbackData(CallbackData.QTY_50)
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
     * Клавиатура подтверждения генерации PDF.
     */
    public static InlineKeyboardMarkup confirmationKeyboard(String generateLabel, String backLabel, String menuLabel) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(generateLabel)
                                .callbackData(CallbackData.GEN_CONFIRM_PDF)
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
     * Клавиатура после выбора предмета: демо-генерация PDF и Главное меню.
     */
    public static InlineKeyboardMarkup demoGenerationKeyboard(String demoLabel, String menuLabel) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(demoLabel)
                                .callbackData(CallbackData.GEN_DEMO_PDF)
                                .build()))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(menuLabel)
                                .callbackData(CallbackData.MENU)
                                .build()))
                .build();
    }

    /**
     * Клавиатура «Назад» и «Главное меню» (например, после заглушки «в разработке»).
     */
    public static InlineKeyboardMarkup backAndMenuKeyboard(String backLabel, String menuLabel) {
        return InlineKeyboardMarkup.builder()
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
