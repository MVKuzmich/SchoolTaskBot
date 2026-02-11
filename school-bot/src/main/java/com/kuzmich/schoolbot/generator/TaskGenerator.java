package com.kuzmich.schoolbot.generator;

import java.util.List;

/**
 * Единый интерфейс для всех генераторов заданий.
 * Каждый генератор принимает базовый контекст и при необходимости приводит к своему типу (например {@link ArithmeticContext}).
 */
public interface TaskGenerator {

    /**
     * Генерирует список заданий по заданному контексту.
     *
     * @param context контекст генерации (реализация {@link GenerationContext}); не null
     * @return список заданий (может быть пустым при quantity = 0)
     */
    List<Task> generate(GenerationContext context);
}
