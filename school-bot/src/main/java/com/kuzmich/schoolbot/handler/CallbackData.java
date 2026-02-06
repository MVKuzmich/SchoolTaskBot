package com.kuzmich.schoolbot.handler;

/**
 * Константы callback_data для inline-кнопок (соответствуют MathBot-Scenarios).
 * Формат: режим/действие_уточнение (макс. 64 байта в Telegram API).
 */
public final class CallbackData {

    private CallbackData() {
    }

    /** Выбор режима: Генератор заданий */
    public static final String MODE_GENERATOR = "mode_generator";
    /** Выбор режима: Тренажёр */
    public static final String MODE_TRAINER = "mode_trainer";

    /** Выбор класса: Начальная школа (1-4) */
    public static final String GEN_ELEMENTARY = "gen_elementary";
    /** Выбор класса: Средняя школа (5-9) */
    public static final String GEN_SECONDARY = "gen_secondary";

    /** Выбор предмета (MVP: только математика) */
    public static final String SUBJECT_MATH = "subject_math";

    /** Возврат к выбору режима */
    public static final String BACK_TO_MODE = "back_to_mode";
    /** Возврат к выбору класса */
    public static final String BACK_TO_CLASS = "back_to_class";
    /** Главное меню (то же, что /menu) */
    public static final String MENU = "menu";
}
