package com.kuzmich.schoolbot.generator;

import com.kuzmich.schoolbot.core.service.MessageService;
import com.kuzmich.schoolbot.core.validation.ValidationException;
import com.kuzmich.schoolbot.generator.arithmetic.Addition20NoCarryGenerator;
import com.kuzmich.schoolbot.generator.arithmetic.AdditionGenerator;
import com.kuzmich.schoolbot.generator.arithmetic.Subtraction20NoCarryGenerator;
import com.kuzmich.schoolbot.generator.arithmetic.SubtractionGenerator;
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
 * Тесты {@link GeneratorFactory}: getGenerator для всех OperationType, null.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GeneratorFactory")
class GeneratorFactoryTest {

    @Mock
    private MessageService messageService;

    private GeneratorFactory factory;

    @BeforeEach
    void setUp() {
        factory = new GeneratorFactory(List.of(
                new AdditionGenerator(messageService),
                new SubtractionGenerator(messageService),
                new Addition20NoCarryGenerator(messageService),
                new Subtraction20NoCarryGenerator(messageService)
        ));
    }

    @Test
    @DisplayName("getGenerator: выбрасывает при null type")
    void getGenerator_throwsWhenTypeNull() {
        assertThatThrownBy(() -> factory.getGenerator(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("type");
    }

    @Test
    @DisplayName("getGenerator(ADDITION_10) возвращает генератор, дающий сложение в пределах 10")
    void getGenerator_addition10_returnsValidTasks() {
        when(messageService.getText(anyString(), any(), any())).thenAnswer(inv -> {
            Object a = inv.getArgument(1);
            Object b = inv.getArgument(2);
            String key = inv.getArgument(0, String.class);
            if (key != null && key.contains("subtraction")) {
                return a + " - " + b + " = ";
            }
            return a + " + " + b + " = ";
        });

        TaskGenerator gen = factory.getGenerator(OperationType.ADDITION_10);
        ArithmeticContext context = ArithmeticContext.builder()
                .operationType(OperationType.ADDITION_10)
                .numberRange(new Range(0, 10))
                .quantity(15)
                .build();

        List<Task> tasks = gen.generate(context);

        assertThat(tasks)
                .hasSize(15)
                .allMatch(t -> {
                    int answer = Integer.parseInt(t.answer());
                    return answer >= 0 && answer <= 10;
                });
        assertThat(tasks.get(0).question()).contains("+");
    }

    @Test
    @DisplayName("getGenerator(SUBTRACTION_10) возвращает генератор вычитания в пределах 10")
    void getGenerator_subtraction10_returnsValidTasks() {
        when(messageService.getText(anyString(), any(), any())).thenAnswer(inv -> {
            Object a = inv.getArgument(1);
            Object b = inv.getArgument(2);
            String key = inv.getArgument(0, String.class);
            if (key != null && key.contains("subtraction")) {
                return a + " - " + b + " = ";
            }
            return a + " + " + b + " = ";
        });

        TaskGenerator gen = factory.getGenerator(OperationType.SUBTRACTION_10);
        ArithmeticContext context = ArithmeticContext.builder()
                .operationType(OperationType.SUBTRACTION_10)
                .numberRange(new Range(0, 10))
                .quantity(15)
                .build();

        List<Task> tasks = gen.generate(context);

        assertThat(tasks)
                .hasSize(15)
                .allMatch(t -> Integer.parseInt(t.answer()) >= 0);
        assertThat(tasks.get(0).question()).contains("-");
    }

    @Test
    @DisplayName("getGenerator(ADDITION_20_NO_CARRY) возвращает генератор сложения без перехода")
    void getGenerator_addition20NoCarry_returnsValidTasks() {
        when(messageService.getText(anyString(), any(), any())).thenAnswer(inv -> {
            Object a = inv.getArgument(1);
            Object b = inv.getArgument(2);
            String key = inv.getArgument(0, String.class);
            if (key != null && key.contains("subtraction")) {
                return a + " - " + b + " = ";
            }
            return a + " + " + b + " = ";
        });

        TaskGenerator gen = factory.getGenerator(OperationType.ADDITION_20_NO_CARRY);
        ArithmeticContext context = ArithmeticContext.builder()
                .operationType(OperationType.ADDITION_20_NO_CARRY)
                .numberRange(new Range(0, 20))
                .quantity(10)
                .build();

        List<Task> tasks = gen.generate(context);

        assertThat(tasks)
                .hasSize(10)
                .allMatch(t -> {
                    String q = t.question();
                    String[] parts = q.replace(" = ", "").split(" \\+ ");
                    int a = Integer.parseInt(parts[0].trim());
                    int b = Integer.parseInt(parts[1].trim());
                    return (a % 10) + (b % 10) < 10;
                });
    }

    @Test
    @DisplayName("getGenerator(SUBTRACTION_20_NO_CARRY) возвращает генератор вычитания без перехода")
    void getGenerator_subtraction20NoCarry_returnsValidTasks() {
        when(messageService.getText(anyString(), any(), any())).thenAnswer(inv -> {
            Object a = inv.getArgument(1);
            Object b = inv.getArgument(2);
            String key = inv.getArgument(0, String.class);
            if (key != null && key.contains("subtraction")) {
                return a + " - " + b + " = ";
            }
            return a + " + " + b + " = ";
        });

        TaskGenerator gen = factory.getGenerator(OperationType.SUBTRACTION_20_NO_CARRY);
        ArithmeticContext context = ArithmeticContext.builder()
                .operationType(OperationType.SUBTRACTION_20_NO_CARRY)
                .numberRange(new Range(0, 20))
                .quantity(10)
                .build();

        List<Task> tasks = gen.generate(context);

        assertThat(tasks)
                .hasSize(10)
                .allMatch(t -> {
                    String q = t.question();
                    String[] parts = q.replace(" = ", "").split(" - ");
                    int a = Integer.parseInt(parts[0].trim());
                    int b = Integer.parseInt(parts[1].trim());
                    return (a % 10) >= (b % 10) && Integer.parseInt(t.answer()) >= 0;
                });
    }
}
