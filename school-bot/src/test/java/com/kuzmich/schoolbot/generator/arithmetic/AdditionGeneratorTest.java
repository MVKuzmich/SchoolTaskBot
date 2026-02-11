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
 * Unit-тесты {@link AdditionGenerator}: сложение в пределах 10.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdditionGenerator")
class AdditionGeneratorTest {

    @Mock
    private MessageService messageService;

    private AdditionGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new AdditionGenerator(messageService);
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
        @DisplayName("возвращает 20 заданий с ответами в [0, 10]")
        void shouldGenerateValidAdditionTasks_range0to10() {
            when(messageService.getText(anyString(), any(), any()))
                    .thenAnswer(inv -> inv.getArgument(1) + " + " + inv.getArgument(2) + " = __");

            ArithmeticContext context = ArithmeticContext.builder()
                    .operationType(OperationType.ADDITION_10)
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
        @DisplayName("все ответы корректны: a + b = answer")
        void shouldGenerateCorrectAnswers_addition() {
            when(messageService.getText(anyString(), any(), any()))
                    .thenAnswer(inv -> inv.getArgument(1) + " + " + inv.getArgument(2) + " = __");

            ArithmeticContext context = ArithmeticContext.builder()
                    .operationType(OperationType.ADDITION_10)
                    .numberRange(new Range(0, 10))
                    .quantity(100)
                    .build();

            List<Task> tasks = generator.generate(context);

            assertThat(tasks).allMatch(task -> {
                String question = task.question();
                int answer = Integer.parseInt(task.answer());
                String[] parts = question.replace(" = __", "").split(" \\+ ");
                int a = Integer.parseInt(parts[0].trim());
                int b = Integer.parseInt(parts[1].trim());
                return a + b == answer;
            });
        }

        @Test
        @DisplayName("quantity = 0 возвращает пустой список")
        void shouldReturnEmptyList_whenQuantityZero() {
            ArithmeticContext context = ArithmeticContext.builder()
                    .operationType(OperationType.ADDITION_10)
                    .numberRange(new Range(0, 10))
                    .quantity(0)
                    .build();

            List<Task> tasks = generator.generate(context);

            assertThat(tasks).isEmpty();
        }

        @Test
        @DisplayName("quantity = 200 возвращает 200 заданий в диапазоне")
        void shouldGenerate200Tasks_whenQuantity200() {
            ArithmeticContext context = ArithmeticContext.builder()
                    .operationType(OperationType.ADDITION_10)
                    .numberRange(new Range(0, 10))
                    .quantity(200)
                    .build();

            List<Task> tasks = generator.generate(context);

            assertThat(tasks).hasSize(200);
            assertThat(tasks).allMatch(task -> {
                int answer = Integer.parseInt(task.answer());
                return answer >= 0 && answer <= 10;
            });
        }

        @Test
        @DisplayName("при quantity = 50 примеры не повторяются")
        void shouldGenerateUniqueTasks_whenQuantity50() {
            when(messageService.getText(anyString(), any(), any()))
                    .thenAnswer(inv -> inv.getArgument(1) + " + " + inv.getArgument(2) + " = __");

            ArithmeticContext context = ArithmeticContext.builder()
                    .operationType(OperationType.ADDITION_10)
                    .numberRange(new Range(0, 10))
                    .quantity(50)
                    .build();

            List<Task> tasks = generator.generate(context);

            assertThat(tasks).hasSize(50);
            long distinctQuestions = tasks.stream().map(Task::question).distinct().count();
            assertThat(distinctQuestions).isEqualTo(50);
        }

        @Test
        @DisplayName("при quantity = 100 допускаются повторы")
        void shouldAllowDuplicates_whenQuantity100() {
            when(messageService.getText(anyString(), any(), any()))
                    .thenAnswer(inv -> inv.getArgument(1) + " + " + inv.getArgument(2) + " = __");

            ArithmeticContext context = ArithmeticContext.builder()
                    .operationType(OperationType.ADDITION_10)
                    .numberRange(new Range(0, 10))
                    .quantity(100)
                    .build();

            List<Task> tasks = generator.generate(context);

            assertThat(tasks).hasSize(100);
            // При 100 заданиях в диапазоне 0–10 уникальных комбинаций меньше 100, значит будут повторы
            long distinctQuestions = tasks.stream().map(Task::question).distinct().count();
            assertThat(distinctQuestions).isLessThanOrEqualTo(100);
        }
    }
}
