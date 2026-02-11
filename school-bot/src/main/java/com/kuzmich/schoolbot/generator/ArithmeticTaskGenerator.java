package com.kuzmich.schoolbot.generator;

/**
 * Генератор арифметических заданий, привязанный к одному типу операции.
 * Позволяет фабрике автоматически регистрировать все бины этого типа.
 */
public interface ArithmeticTaskGenerator extends TaskGenerator {

    /**
     * Тип операции, который обрабатывает этот генератор.
     */
    OperationType getOperationType();
}
