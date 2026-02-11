package com.kuzmich.schoolbot.generator;

import com.kuzmich.schoolbot.core.validation.Validation;
import lombok.Builder;
import lombok.Getter;

/**
 * Контекст генерации арифметических заданий (1–4 класс): сложение/вычитание, диапазон, флаг «без перехода».
 */
@Getter
@Builder
public class ArithmeticContext implements GenerationContext {

    private final OperationType operationType;
    private final Range numberRange;
    private final int quantity;
    private final Boolean noCarry;

    @Override
    public ArithmeticContext validate() {
        Validation.requireNonNull(operationType, "operationType");
        Validation.requireNonNull(numberRange, "numberRange");
        if (quantity < 0) {
            throw new com.kuzmich.schoolbot.core.validation.ValidationException(
                    "quantity не может быть отрицательным, получено: " + quantity);
        }
        return this;
    }
}
