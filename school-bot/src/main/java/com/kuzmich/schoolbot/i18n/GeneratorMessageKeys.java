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
    /** Сообщение «Темы скоро». */
    public static final String TOPIC_COMING = "generator.topic.coming";
    /** Текст справки (кнопка «Справка»). */
    public static final String HELP_MESSAGE = "help.message";

    /** Формат вопроса сложения: {0} + {1} = __ */
    public static final String QUESTION_FORMAT_ADDITION = "generator.arithmetic.question.format.addition";
    /** Формат вопроса вычитания: {0} - {1} = __ */
    public static final String QUESTION_FORMAT_SUBTRACTION = "generator.arithmetic.question.format.subtraction";
}
