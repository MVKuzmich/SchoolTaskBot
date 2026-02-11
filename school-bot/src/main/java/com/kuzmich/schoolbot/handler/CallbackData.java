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

    /** Демо-генерация PDF после выбора предмета. */
    public static final String GEN_DEMO_PDF = "gen_demo_pdf";

    /** Тема: Арифметика (сложение/вычитание). */
    public static final String TOPIC_ARITHMETIC = "topic_arithmetic";

    /** Тип операции: сложение 0-10. */
    public static final String OP_ADDITION_10 = "op_add_10";
    /** Тип операции: вычитание 0-10. */
    public static final String OP_SUBTRACTION_10 = "op_sub_10";
    /** Тип операции: сложение до 20 без перехода через десяток. */
    public static final String OP_ADDITION_20_NO_CARRY = "op_add_20_nc";
    /** Тип операции: вычитание до 20 без перехода через десяток. */
    public static final String OP_SUBTRACTION_20_NO_CARRY = "op_sub_20_nc";

    /** Количество примеров: 10. */
    public static final String QTY_10 = "qty_10";
    /** Количество примеров: 20. */
    public static final String QTY_20 = "qty_20";

    /** Подтверждение генерации PDF с выбранными параметрами. */
    public static final String GEN_CONFIRM_PDF = "gen_confirm_pdf";

    /** Возврат к выбору режима */
    public static final String BACK_TO_MODE = "back_to_mode";
    /** Возврат к выбору класса */
    public static final String BACK_TO_CLASS = "back_to_class";
    /** Главное меню (то же, что /menu) */
    public static final String MENU = "menu";
}
