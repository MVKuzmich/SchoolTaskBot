package com.kuzmich.schoolbot.generator.numbers;

import com.kuzmich.schoolbot.core.service.MessageService;
import com.kuzmich.schoolbot.generator.ArithmeticContext;
import com.kuzmich.schoolbot.generator.OperationType;
import com.kuzmich.schoolbot.generator.Task;
import com.kuzmich.schoolbot.i18n.GeneratorMessageKeys;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link ComparisonGenerator}: сравнение чисел (знаки &lt;, &gt;, =).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ComparisonGenerator")
class ComparisonGeneratorTest {

    @Mock
    private MessageService messageService;

    private ComparisonGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new ComparisonGenerator(messageService);
    }

    private void stubMessageService() {
        when(messageService.getText(eq(GeneratorMessageKeys.QUESTION_BLANK))).thenReturn("  ");
        when(messageService.getText(eq(GeneratorMessageKeys.FORMAT_COMPARISON), any(), any(), any()))
                .thenAnswer(inv -> inv.getArgument(1) + " " + inv.getArgument(2) + " " + inv.getArgument(3));
    }

    @Test
    @DisplayName("generate: выбрасывает при null context")
    void generate_throwsWhenContextNull() {
        assertThatThrownBy(() -> generator.generate(null))
                .isInstanceOf(Exception.class);
    }

    @Nested
    @DisplayName("корректность заданий")
    class Correctness {

        @Test
        @DisplayName("возвращает заданное количество заданий")
        void shouldGenerateRequestedQuantity() {
            stubMessageService();
            ArithmeticContext context = ArithmeticContext.builder()
                    .operationType(OperationType.COMPARISON)
                    .quantity(30)
                    .build();

            List<Task> tasks = generator.generate(context);

            assertThat(tasks).hasSize(30);
        }

        @Test
        @DisplayName("формат вопроса: a пусто b")
        void shouldHaveCorrectQuestionFormat() {
            stubMessageService();
            ArithmeticContext context = ArithmeticContext.builder()
                    .operationType(OperationType.COMPARISON)
                    .quantity(20)
                    .build();

            List<Task> tasks = generator.generate(context);

            assertThat(tasks)
                    .allMatch(task -> task.question().matches("\\d+\\s+\\d+"));
        }

        @Test
        @DisplayName("ответ корректен: <, > или =")
        void shouldGenerateCorrectComparison() {
            stubMessageService();
            ArithmeticContext context = ArithmeticContext.builder()
                    .operationType(OperationType.COMPARISON)
                    .quantity(50)
                    .build();

            List<Task> tasks = generator.generate(context);

            assertThat(tasks).allMatch(task -> {
                String question = task.question();
                String answer = task.answer();
                String[] parts = question.split("\\s+");
                int a = Integer.parseInt(parts[0].trim());
                int b = Integer.parseInt(parts[1].trim());
                if (a < b) return "<".equals(answer);
                if (a > b) return ">".equals(answer);
                return "=".equals(answer);
            });
        }

        @Test
        @DisplayName("ответ только <, > или =")
        void shouldHaveValidSignAnswer() {
            stubMessageService();
            ArithmeticContext context = ArithmeticContext.builder()
                    .operationType(OperationType.COMPARISON)
                    .quantity(30)
                    .build();

            List<Task> tasks = generator.generate(context);

            assertThat(tasks)
                    .allMatch(task -> {
                        String ans = task.answer();
                        return "<".equals(ans) || ">".equals(ans) || "=".equals(ans);
                    });
        }

        @Test
        @DisplayName("при quantity = 0 возвращает пустой список")
        void shouldReturnEmptyList_whenQuantityZero() {
            ArithmeticContext context = ArithmeticContext.builder()
                    .operationType(OperationType.COMPARISON)
                    .quantity(0)
                    .build();

            List<Task> tasks = generator.generate(context);

            assertThat(tasks).isEmpty();
        }
    }
}
