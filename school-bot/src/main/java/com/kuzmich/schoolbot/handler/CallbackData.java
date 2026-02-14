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
    /** Тема: Числа и счёт (состав числа, сравнение, ряд). */
    public static final String TOPIC_NUMBERS = "topic_numbers";

    /** Тип операции: сложение 0-10. */
    public static final String OP_ADDITION_10 = "op_add_10";
    /** Тип операции: вычитание 0-10. */
    public static final String OP_SUBTRACTION_10 = "op_sub_10";
    /** Тип операции: сложение до 20 без перехода через десяток. */
    public static final String OP_ADDITION_20_NO_CARRY = "op_add_20_nc";
    /** Тип операции: вычитание до 20 без перехода через десяток. */
    public static final String OP_SUBTRACTION_20_NO_CARRY = "op_sub_20_nc";
    /** Тип задания: состав числа — открывает подменю выбора варианта (2–9, 10, 11–20, смешанный). */
    public static final String OP_NUMBER_COMPOSITION = "op_number_composition";
    /** Состав числа 2–9. */
    public static final String OP_NUMBER_COMPOSITION_2_9 = "op_comp_2_9";
    /** Состав числа 10. */
    public static final String OP_NUMBER_COMPOSITION_10 = "op_comp_10";
    /** Состав числа 11–20. */
    public static final String OP_NUMBER_COMPOSITION_11_20 = "op_comp_11_20";
    /** Состав числа смешанный (2–10). */
    public static final String OP_NUMBER_COMPOSITION_MIXED = "op_comp_mixed";
    /** Тип задания: сравнение чисел. */
    public static final String OP_COMPARISON = "op_comparison";
    /** Тип задания: продолжи числовой ряд. */
    public static final String OP_NUMBER_SEQUENCE = "op_number_sequence";

    /** Количество примеров: 10. */
    public static final String QTY_10 = "qty_10";
    /** Количество примеров: 20. */
    public static final String QTY_20 = "qty_20";
    /** Количество примеров: 30. */
    public static final String QTY_30 = "qty_30";
    /** Количество примеров: 50. */
    public static final String QTY_50 = "qty_50";

    /** Подтверждение генерации PDF с выбранными параметрами. */
    public static final String GEN_CONFIRM_PDF = "gen_confirm_pdf";

    /** Возврат к выбору режима */
    public static final String BACK_TO_MODE = "back_to_mode";
    /** Возврат к выбору класса */
    public static final String BACK_TO_CLASS = "back_to_class";
    /** Главное меню (то же, что /menu) */
    public static final String MENU = "menu";
}
