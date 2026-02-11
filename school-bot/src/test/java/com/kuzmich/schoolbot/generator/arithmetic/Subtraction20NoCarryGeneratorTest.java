package com.kuzmich.schoolbot.generator.arithmetic;

import com.kuzmich.schoolbot.core.service.MessageService;
import com.kuzmich.schoolbot.generator.ArithmeticContext;
import com.kuzmich.schoolbot.generator.OperationType;
import com.kuzmich.schoolbot.generator.Range;
import com.kuzmich.schoolbot.generator.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link Subtraction20NoCarryGenerator}: вычитание до 20 без перехода через десяток.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Subtraction20NoCarryGenerator")
class Subtraction20NoCarryGeneratorTest {

    @Mock
    private MessageService messageService;

    private Subtraction20NoCarryGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new Subtraction20NoCarryGenerator(messageService);
    }

    @Test
    @DisplayName("generate: выбрасывает при null context")
    void generate_throwsWhenContextNull() {
        assertThatThrownBy(() -> generator.generate(null))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("все примеры без перехода через десяток, ответ неотрицательный")
    void shouldNotCrossDecimal_subtraction20() {
        when(messageService.getText(anyString(), any(), any()))
                .thenAnswer(inv -> inv.getArgument(1) + " - " + inv.getArgument(2) + " = __");

        ArithmeticContext context = ArithmeticContext.builder()
                .operationType(OperationType.SUBTRACTION_20_NO_CARRY)
                .numberRange(new Range(0, 20))
                .noCarry(true)
                .quantity(50)
                .build();

        List<Task> tasks = generator.generate(context);

        assertThat(tasks).hasSize(50);
        assertThat(tasks).allMatch(task -> {
            String question = task.question();
            String[] parts = question.replace(" = __", "").split(" - ");
            int a = Integer.parseInt(parts[0].trim());
            int b = Integer.parseInt(parts[1].trim());
            int answer = Integer.parseInt(task.answer());
            boolean diffCorrect = a - b == answer;
            boolean noCarry = (a % 10) >= (b % 10);
            boolean nonNegative = answer >= 0;
            return diffCorrect && noCarry && nonNegative;
        });
    }

    @Test
    @DisplayName("quantity = 0 возвращает пустой список")
    void shouldReturnEmptyList_whenQuantityZero() {
        ArithmeticContext context = ArithmeticContext.builder()
                .operationType(OperationType.SUBTRACTION_20_NO_CARRY)
                .numberRange(new Range(0, 20))
                .quantity(0)
                .build();

        List<Task> tasks = generator.generate(context);

        assertThat(tasks).isEmpty();
    }
}
