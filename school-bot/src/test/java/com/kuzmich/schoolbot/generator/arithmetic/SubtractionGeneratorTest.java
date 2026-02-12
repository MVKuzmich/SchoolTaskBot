package com.kuzmich.schoolbot.generator.arithmetic;

import com.kuzmich.schoolbot.core.service.MessageService;
import com.kuzmich.schoolbot.generator.ArithmeticContext;
import com.kuzmich.schoolbot.generator.OperationType;
import com.kuzmich.schoolbot.generator.Range;
import com.kuzmich.schoolbot.generator.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
 * Unit-тесты {@link SubtractionGenerator}: вычитание в пределах 10.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SubtractionGenerator")
class SubtractionGeneratorTest {

    @Mock
    private MessageService messageService;

    private SubtractionGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new SubtractionGenerator(messageService);
    }

    @Test
    @DisplayName("generate: выбрасывает при null context")
    void generate_throwsWhenContextNull() {
        assertThatThrownBy(() -> generator.generate(null))
                .isInstanceOf(Exception.class);
    }

    @Nested
    @DisplayName("диапазон 0–10")
    class Range0To10 {

        @Test
        @DisplayName("возвращает заданное количество заданий, ответы неотрицательные")
        void shouldGenerateValidSubtractionTasks_range0to10() {
            ArithmeticContext context = ArithmeticContext.builder()
                    .operationType(OperationType.SUBTRACTION_10)
                    .numberRange(new Range(0, 10))
                    .quantity(20)
                    .build();

            List<Task> tasks = generator.generate(context);

            assertThat(tasks)
                    .hasSize(20)
                    .allMatch(task -> {
                        int answer = Integer.parseInt(task.answer());
                        return answer >= 0 && answer <= 10;
                    });
        }

        @Test
        @DisplayName("все ответы корректны: a - b = answer, результат не отрицательный")
        void shouldGenerateCorrectAnswers_subtraction() {
            when(messageService.getText(anyString(), any(), any()))
                    .thenAnswer(inv -> inv.getArgument(1) + " - " + inv.getArgument(2) + " = __");

            ArithmeticContext context = ArithmeticContext.builder()
                    .operationType(OperationType.SUBTRACTION_10)
                    .numberRange(new Range(0, 10))
                    .quantity(100)
                    .build();

            List<Task> tasks = generator.generate(context);

            assertThat(tasks)
                    .isNotEmpty()
                    .allMatch(task -> {
                        String question = task.question();
                        int answer = Integer.parseInt(task.answer());
                        String[] parts = question.replace(" = __", "").split(" - ");
                        int a = Integer.parseInt(parts[0].trim());
                        int b = Integer.parseInt(parts[1].trim());
                        return a - b == answer && answer >= 0;
                    });
        }

        @Test
        @DisplayName("quantity = 0 возвращает пустой список")
        void shouldReturnEmptyList_whenQuantityZero() {
            ArithmeticContext context = ArithmeticContext.builder()
                    .operationType(OperationType.SUBTRACTION_10)
                    .numberRange(new Range(0, 10))
                    .quantity(0)
                    .build();

            List<Task> tasks = generator.generate(context);

            assertThat(tasks).isEmpty();
        }

        /** Размер домена вычитания 0..10: a∈[0,10], b∈[0,a] → 66 уникальных пар. */
        private static final int SUBTRACTION_DOMAIN_SIZE_0_10 = 66;

        @Test
        @DisplayName("при quantity больше размера домена возвращает все уникальные примеры (без дублей)")
        void shouldGenerateAtMostDomainSize_whenQuantity200() {
            when(messageService.getText(anyString(), any(), any()))
                    .thenAnswer(inv -> inv.getArgument(1) + " - " + inv.getArgument(2) + " = __");

            ArithmeticContext context = ArithmeticContext.builder()
                    .operationType(OperationType.SUBTRACTION_10)
                    .numberRange(new Range(0, 10))
                    .quantity(200)
                    .build();

            List<Task> tasks = generator.generate(context);

            assertThat(tasks)
                    .hasSizeLessThanOrEqualTo(SUBTRACTION_DOMAIN_SIZE_0_10)
                    .allMatch(task -> Integer.parseInt(task.answer()) >= 0);
            long distinctQuestions = tasks.stream().map(Task::question).distinct().count();
            assertThat(distinctQuestions).isEqualTo(tasks.size());
        }
    }
}
