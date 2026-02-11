package com.kuzmich.schoolbot.generator;

/**
 * Базовый контракт контекста генерации заданий.
 * Конкретные домены (арифметика, дроби, состав числа и т.д.) имеют свои реализации.
 */
public interface GenerationContext {

    /**
     * Количество заданий для генерации.
     */
    int getQuantity();

    /**
     * Валидирует контекст. Вызывается генератором перед работой.
     *
     * @return этот контекст при успешной проверке
     * @throws com.kuzmich.schoolbot.core.validation.ValidationException при невалидных значениях
     */
    GenerationContext validate();
}
