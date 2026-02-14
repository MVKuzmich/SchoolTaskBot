package com.kuzmich.schoolbot.generator;

/**
 * Генератор арифметических заданий (сложение/вычитание), привязанный к одному типу операции.
 * Расширяет {@link OperationTaskGenerator} для регистрации в {@link GeneratorFactory}.
 */
public interface ArithmeticTaskGenerator extends OperationTaskGenerator {
}
