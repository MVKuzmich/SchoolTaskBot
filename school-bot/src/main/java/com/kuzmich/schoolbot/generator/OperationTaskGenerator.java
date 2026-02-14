package com.kuzmich.schoolbot.generator;

/**
 * Генератор заданий, привязанный к одному типу операции (арифметика или числа/счёт).
 * Позволяет {@link GeneratorFactory} регистрировать все бины по типу операции.
 */
public interface OperationTaskGenerator extends TaskGenerator {

    /**
     * Тип операции, который обрабатывает этот генератор.
     */
    OperationType getOperationType();
}
