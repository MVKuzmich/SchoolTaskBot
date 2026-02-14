package com.kuzmich.schoolbot.generator;

import com.kuzmich.schoolbot.core.validation.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тесты {@link ArithmeticContext}: builder, валидация, null-параметры.
 */
@DisplayName("ArithmeticContext")
class ArithmeticContextTest {

    @Test
    @DisplayName("validate: успех при корректных полях")
    void validate_successWhenValid() {
        ArithmeticContext ctx = ArithmeticContext.builder()
                .operationType(OperationType.ADDITION_10)
                .numberRange(new Range(0, 10))
                .quantity(20)
                .build();
        assertThatCode(ctx::validate).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validate: исключение при null operationType")
    void validate_throwsWhenOperationTypeNull() {
        ArithmeticContext ctx = ArithmeticContext.builder()
                .operationType(null)
                .numberRange(new Range(0, 10))
                .quantity(10)
                .build();
        assertThatThrownBy(ctx::validate)
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("operationType");
    }

    @Test
    @DisplayName("validate: исключение при null numberRange")
    void validate_throwsWhenNumberRangeNull() {
        ArithmeticContext ctx = ArithmeticContext.builder()
                .operationType(OperationType.ADDITION_10)
                .numberRange(null)
                .quantity(10)
                .build();
        assertThatThrownBy(ctx::validate)
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("numberRange");
    }

    @Test
    @DisplayName("validate: исключение при отрицательном quantity")
    void validate_throwsWhenQuantityNegative() {
        ArithmeticContext ctx = ArithmeticContext.builder()
                .operationType(OperationType.ADDITION_10)
                .numberRange(new Range(0, 10))
                .quantity(-1)
                .build();
        assertThatThrownBy(ctx::validate)
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("quantity");
    }

    @Test
    @DisplayName("validate: успех при quantity = 0")
    void validate_successWhenQuantityZero() {
        ArithmeticContext ctx = ArithmeticContext.builder()
                .operationType(OperationType.SUBTRACTION_10)
                .numberRange(new Range(0, 10))
                .quantity(0)
                .build();
        assertThatCode(ctx::validate).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validate: успех при NUMBER_COMPOSITION и null numberRange")
    void validate_successWhenNumberCompositionAndNullRange() {
        ArithmeticContext ctx = ArithmeticContext.builder()
                .operationType(OperationType.NUMBER_COMPOSITION)
                .numberRange(null)
                .quantity(10)
                .build();
        assertThatCode(ctx::validate).doesNotThrowAnyException();
    }
}
