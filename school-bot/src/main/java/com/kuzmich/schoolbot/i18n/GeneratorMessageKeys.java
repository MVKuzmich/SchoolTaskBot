package com.kuzmich.schoolbot.i18n;

/**
 * Ключи сообщений сценария генератора заданий (режим → класс → предмет).
 * Используются в {@link com.kuzmich.schoolbot.handler.GeneratorCallbackHandler} и клавиатурах.
 * Значения берутся из messages*.properties через MessageSource.
 */
public final class GeneratorMessageKeys {

    private GeneratorMessageKeys() {
    }

    /** Заголовок выбора класса (начальная / средняя школа). */
    public static final String GENERATOR_CLASS_TITLE = "generator.class.title";
    /** Заголовок выбора предмета. */
    public static final String GENERATOR_SUBJECT_TITLE = "generator.subject.title";
    /** Сообщение «Тренажёр скоро». */
    public static final String TRAINER_COMING_SOON = "trainer.coming.soon";
    /** Сообщение «Раздел 5–9 класс в разработке». */
    public static final String CLASS_SECONDARY_COMING_SOON = "generator.class.secondary.coming.soon";

    /** Подпись кнопки «Назад». */
    public static final String BUTTON_BACK = "button.back";
    /** Подпись кнопки «Главное меню». */
    public static final String BUTTON_MENU = "button.menu";
    /** Подпись кнопки «Справка». */
    public static final String BUTTON_HELP = "button.help";

    /** Подпись варианта «Начальная школа» (1–4 класс). */
    public static final String CLASS_ELEMENTARY = "generator.class.elementary";
    /** Подпись варианта «Средняя школа» (5–9 класс). */
    public static final String CLASS_SECONDARY = "generator.class.secondary";
    /** Подпись предмета «Математика». */
    public static final String SUBJECT_MATH = "generator.subject.math";
    /** Заголовок выбора темы (арифметика и т.п.). */
    public static final String TOPIC_TITLE = "generator.topic.title";
    /** Тема «Арифметика». */
    public static final String TOPIC_ARITHMETIC = "generator.topic.arithmetic";
    /** Тема «Числа и счёт» (с emoji для Telegram). */
    public static final String TOPIC_NUMBERS = "generator.topic.numbers";
    /** Тема «Числа и счёт» без emoji (для заголовка PDF). */
    public static final String TOPIC_NUMBERS_LABEL = "generator.topic.numbers.label";
    /** Текст справки (кнопка «Справка»). */
    public static final String HELP_MESSAGE = "help.message";

    /** Подпись кнопки демо-генерации PDF. */
    public static final String BUTTON_PDF_DEMO = "generator.button.demo.pdf";
    /** Заголовок демо-PDF. */
    public static final String PDF_DEMO_TITLE = "generator.pdf.demo.title";
    /** Сообщение об ошибке генерации PDF. */
    public static final String PDF_GENERATION_ERROR = "generator.pdf.generation.error";

    /** Заголовок выбора типа операции (арифметика). */
    public static final String OPERATION_TITLE = "generator.operation.title";
    /** Заголовок выбора типа задания (числа и счёт). */
    public static final String OPERATION_TITLE_NUMBERS = "generator.operation.title.numbers";
    public static final String OPERATION_ADDITION_10 = "generator.operation.addition10";
    public static final String OPERATION_SUBTRACTION_10 = "generator.operation.subtraction10";
    public static final String OPERATION_ADDITION_20_NO_CARRY = "generator.operation.addition20.no.carry";
    public static final String OPERATION_SUBTRACTION_20_NO_CARRY = "generator.operation.subtraction20.no.carry";
    /** Состав числа (кнопка, открывающая подменю вариантов). */
    public static final String OPERATION_NUMBER_COMPOSITION = "generator.operation.number.composition";
    /** Заголовок подменю «Выбери вариант состава числа». */
    public static final String OPERATION_NUMBER_COMPOSITION_TITLE = "generator.operation.number.composition.title";
    /** Состав числа 2–9. */
    public static final String OPERATION_NUMBER_COMPOSITION_2_9 = "generator.operation.number.composition.2_9";
    /** Состав числа 10. */
    public static final String OPERATION_NUMBER_COMPOSITION_10 = "generator.operation.number.composition.10";
    /** Состав числа 11–20. */
    public static final String OPERATION_NUMBER_COMPOSITION_11_20 = "generator.operation.number.composition.11_20";
    /** Состав числа смешанный (2–10). */
    public static final String OPERATION_NUMBER_COMPOSITION_MIXED = "generator.operation.number.composition.mixed";
    /** Сравнение чисел. */
    public static final String OPERATION_COMPARISON = "generator.operation.comparison";
    /** Продолжи числовой ряд. */
    public static final String OPERATION_NUMBER_SEQUENCE = "generator.operation.number.sequence";

    /** Заголовок выбора количества примеров. */
    public static final String QUANTITY_TITLE = "generator.quantity.title";
    public static final String QUANTITY_10 = "generator.quantity.10";
    public static final String QUANTITY_20 = "generator.quantity.20";
    public static final String QUANTITY_30 = "generator.quantity.30";
    public static final String QUANTITY_50 = "generator.quantity.50";

    /** Заголовок подтверждения генерации и кнопка «Создать PDF». */
    public static final String CONFIRM_TITLE = "generator.confirm.title";
    public static final String BUTTON_GENERATE_PDF = "generator.button.generate.pdf";

    /** Плейсхолдер пустого места в задании (равнозначен ширине __, без подчёркивания). */
    public static final String QUESTION_BLANK = "generator.question.blank";
    /** Формат вопроса сложения: {0} + {1} = {2}, где {2} — пустое место. */
    public static final String QUESTION_FORMAT_ADDITION = "generator.arithmetic.question.format.addition";
    /** Формат вопроса вычитания: {0} - {1} = {2}, где {2} — пустое место. */
    public static final String QUESTION_FORMAT_SUBTRACTION = "generator.arithmetic.question.format.subtraction";
    /** Формат «состав числа»: скрыто первое слагаемое — {0}=n, {1}=пусто, {2}=видимое. */
    public static final String FORMAT_COMPOSITION_HIDE_FIRST = "generator.numbers.composition.hide.first";
    /** Формат «состав числа»: скрыто второе слагаемое — {0}=n, {1}=видимое, {2}=пусто. */
    public static final String FORMAT_COMPOSITION_HIDE_SECOND = "generator.numbers.composition.hide.second";
    /** Формат «сравнение»: {0}=первое число, {1}=пусто, {2}=второе число. */
    public static final String FORMAT_COMPARISON = "generator.numbers.comparison";
}
