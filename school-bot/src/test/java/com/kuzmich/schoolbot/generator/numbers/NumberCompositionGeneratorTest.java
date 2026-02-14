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
 * Unit-тесты {@link NumberCompositionGenerator}: состав числа до 10.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NumberCompositionGenerator")
class NumberCompositionGeneratorTest {

    private static final String BLANK = "  ";

    @Mock
    private MessageService messageService;

    private NumberCompositionGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new NumberCompositionGenerator(messageService);
    }

    private void stubMessageService() {
        when(messageService.getText(eq(GeneratorMessageKeys.QUESTION_BLANK))).thenReturn(BLANK);
        when(messageService.getText(eq(GeneratorMessageKeys.FORMAT_COMPOSITION_HIDE_FIRST), any(), any(), any()))
                .thenAnswer(inv -> inv.getArgument(1) + " = " + inv.getArgument(2) + " + " + inv.getArgument(3));
        when(messageService.getText(eq(GeneratorMessageKeys.FORMAT_COMPOSITION_HIDE_SECOND), any(), any(), any()))
                .thenAnswer(inv -> inv.getArgument(1) + " = " + inv.getArgument(2) + " + " + inv.getArgument(3));
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
                    .operationType(OperationType.NUMBER_COMPOSITION)
                    .quantity(20)
                    .build();

            List<Task> tasks = generator.generate(context);

            assertThat(tasks).hasSize(20);
        }

        @Test
        @DisplayName("формат вопроса: N = пусто + M или N = M + пусто")
        void shouldHaveCorrectQuestionFormat() {
            stubMessageService();
            ArithmeticContext context = ArithmeticContext.builder()
                    .operationType(OperationType.NUMBER_COMPOSITION)
                    .quantity(30)
                    .build();

            List<Task> tasks = generator.generate(context);

            assertThat(tasks)
                    .allMatch(task -> {
                        String q = task.question();
                        return (q.matches("\\d+ = \\s+ \\+ \\d+") || q.matches("\\d+ = \\d+ \\+ \\s+"));
                    });
        }

        @Test
        @DisplayName("ответ совпадает с пропущенным слагаемым")
        void shouldGenerateCorrectComposition() {
            stubMessageService();
            ArithmeticContext context = ArithmeticContext.builder()
                    .operationType(OperationType.NUMBER_COMPOSITION)
                    .quantity(50)
                    .build();

            List<Task> tasks = generator.generate(context);

            assertThat(tasks).allMatch(task -> {
                String question = task.question();
                int answer = Integer.parseInt(task.answer());
                String[] parts = question.split("\\s*=\\s*", 2);
                if (parts.length < 2) return false;
                int n = Integer.parseInt(parts[0].trim());
                String[] addends = parts[1].trim().split("\\s*\\+\\s*", 2);
                if (addends.length < 2) return false;
                int visible = -1;
                for (int i = 0; i < 2; i++) {
                    String s = addends[i].trim();
                    if (s.matches("\\d+")) {
                        visible = Integer.parseInt(s);
                        break;
                    }
                }
                if (visible < 0) return false;
                return visible + answer == n;
            });
        }

        @Test
        @DisplayName("число n в [2, 10], ответ в [0, 10]")
        void shouldRespectNumberRange() {
            stubMessageService();
            ArithmeticContext context = ArithmeticContext.builder()
                    .operationType(OperationType.NUMBER_COMPOSITION)
                    .quantity(40)
                    .build();

            List<Task> tasks = generator.generate(context);

            assertThat(tasks).allMatch(task -> {
                String q = task.question();
                int answer = Integer.parseInt(task.answer());
                int n = Integer.parseInt(q.split(" = ")[0].trim());
                return n >= 2 && n <= 10 && answer >= 0 && answer <= 10;
            });
        }

        @Test
        @DisplayName("при quantity = 0 возвращает пустой список")
        void shouldReturnEmptyList_whenQuantityZero() {
            ArithmeticContext context = ArithmeticContext.builder()
                    .operationType(OperationType.NUMBER_COMPOSITION)
                    .quantity(0)
                    .build();

            List<Task> tasks = generator.generate(context);

            assertThat(tasks).isEmpty();
        }

        @Test
        @DisplayName("примеры не повторяются при quantity <= 50")
        void shouldGenerateUniqueTasks_whenQuantity50() {
            stubMessageService();
            ArithmeticContext context = ArithmeticContext.builder()
                    .operationType(OperationType.NUMBER_COMPOSITION)
                    .quantity(50)
                    .build();

            List<Task> tasks = generator.generate(context);

            assertThat(tasks).hasSize(50);
            long distinct = tasks.stream().map(Task::question).distinct().count();
            assertThat(distinct).isEqualTo(50);
        }
    }
}
