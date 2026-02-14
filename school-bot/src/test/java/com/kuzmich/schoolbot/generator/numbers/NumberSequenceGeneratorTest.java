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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link NumberSequenceGenerator}: продолжи числовой ряд.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NumberSequenceGenerator")
class NumberSequenceGeneratorTest {

    private static final String BLANK = "  ";

    @Mock
    private MessageService messageService;

    private NumberSequenceGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new NumberSequenceGenerator(messageService);
    }

    private void stubMessageService() {
        when(messageService.getText(eq(GeneratorMessageKeys.QUESTION_BLANK))).thenReturn(BLANK);
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
                    .operationType(OperationType.NUMBER_SEQUENCE)
                    .quantity(20)
                    .build();

            List<Task> tasks = generator.generate(context);

            assertThat(tasks).hasSize(20);
        }

        @Test
        @DisplayName("формат: пять элементов через запятую, один — пустое место")
        void shouldHaveCorrectQuestionFormat() {
            stubMessageService();
            ArithmeticContext context = ArithmeticContext.builder()
                    .operationType(OperationType.NUMBER_SEQUENCE)
                    .quantity(25)
                    .build();

            List<Task> tasks = generator.generate(context);

            assertThat(tasks).allMatch(task -> {
                String q = task.question();
                String[] parts = q.split(", ");
                if (parts.length != 5) return false;
                int nonNumberCount = 0;
                for (String p : parts) {
                    if (p.trim().matches("\\d+")) continue;
                    nonNumberCount++;
                }
                return nonNumberCount == 1;
            });
        }

        @Test
        @DisplayName("ответ — пропущенное число в ряду")
        void shouldGenerateCorrectSequenceAnswer() {
            stubMessageService();
            ArithmeticContext context = ArithmeticContext.builder()
                    .operationType(OperationType.NUMBER_SEQUENCE)
                    .quantity(30)
                    .build();

            List<Task> tasks = generator.generate(context);

            assertThat(tasks).allMatch(task -> {
                String q = task.question();
                int answer = Integer.parseInt(task.answer());
                String[] parts = q.split(", ");
                int gapIndex = -1;
                for (int i = 0; i < parts.length; i++) {
                    if (!parts[i].trim().matches("\\d+")) {
                        gapIndex = i;
                        break;
                    }
                }
                if (gapIndex < 0) return false;
                int start = gapIndex == 0 ? answer : Integer.parseInt(parts[0].trim());
                return answer == start + gapIndex;
            });
        }

        @Test
        @DisplayName("ряд из 5 последовательных чисел, ответ в [0, 19]")
        void shouldRespectRange() {
            stubMessageService();
            ArithmeticContext context = ArithmeticContext.builder()
                    .operationType(OperationType.NUMBER_SEQUENCE)
                    .quantity(40)
                    .build();

            List<Task> tasks = generator.generate(context);

            assertThat(tasks).allMatch(task -> {
                int answer = Integer.parseInt(task.answer());
                return answer >= 0 && answer <= 19;
            });
        }

        @Test
        @DisplayName("при quantity = 0 возвращает пустой список")
        void shouldReturnEmptyList_whenQuantityZero() {
            ArithmeticContext context = ArithmeticContext.builder()
                    .operationType(OperationType.NUMBER_SEQUENCE)
                    .quantity(0)
                    .build();

            List<Task> tasks = generator.generate(context);

            assertThat(tasks).isEmpty();
        }

        /** Домен: 16 начал × 5 позиций пропуска = 80 уникальных заданий. */
        @Test
        @DisplayName("при quantity > размера домена возвращает не более 80 заданий без повторов")
        void shouldCapAtDomainSize_whenQuantity100() {
            stubMessageService();
            ArithmeticContext context = ArithmeticContext.builder()
                    .operationType(OperationType.NUMBER_SEQUENCE)
                    .quantity(100)
                    .build();

            List<Task> tasks = generator.generate(context);

            assertThat(tasks).hasSizeLessThanOrEqualTo(80);
            long distinct = tasks.stream().map(Task::question).distinct().count();
            assertThat(distinct).isEqualTo(tasks.size());
        }
    }
}
