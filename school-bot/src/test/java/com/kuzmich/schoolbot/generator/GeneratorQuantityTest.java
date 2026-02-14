package com.kuzmich.schoolbot.generator;

import com.kuzmich.schoolbot.core.service.MessageService;
import com.kuzmich.schoolbot.generator.arithmetic.Addition20NoCarryGenerator;
import com.kuzmich.schoolbot.generator.arithmetic.AdditionGenerator;
import com.kuzmich.schoolbot.generator.arithmetic.Subtraction20NoCarryGenerator;
import com.kuzmich.schoolbot.generator.arithmetic.SubtractionGenerator;
import com.kuzmich.schoolbot.generator.numbers.ComparisonGenerator;
import com.kuzmich.schoolbot.generator.numbers.NumberComposition10Generator;
import com.kuzmich.schoolbot.generator.numbers.NumberComposition11To20Generator;
import com.kuzmich.schoolbot.generator.numbers.NumberComposition2To9Generator;
import com.kuzmich.schoolbot.generator.numbers.NumberCompositionGenerator;
import com.kuzmich.schoolbot.generator.numbers.NumberSequenceGenerator;
import com.kuzmich.schoolbot.i18n.GeneratorMessageKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Тесты: для каждого генератора количество сгенерированных заданий совпадает с запрошенным пользователем.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Генераторы: количество заданий по запросу")
class GeneratorQuantityTest {

    @Mock
    private MessageService messageService;

    private GeneratorFactory factory;

    @BeforeEach
    void setUp() {
        factory = new GeneratorFactory(List.of(
                new AdditionGenerator(messageService),
                new SubtractionGenerator(messageService),
                new Addition20NoCarryGenerator(messageService),
                new Subtraction20NoCarryGenerator(messageService),
                new NumberComposition2To9Generator(messageService),
                new NumberComposition10Generator(messageService),
                new NumberComposition11To20Generator(messageService),
                new NumberCompositionGenerator(messageService),
                new ComparisonGenerator(messageService),
                new NumberSequenceGenerator(messageService)
        ));
        stubMessageServiceForAllGenerators();
    }

    private void stubMessageServiceForAllGenerators() {
        when(messageService.getText(GeneratorMessageKeys.QUESTION_BLANK)).thenReturn("  ");
        // Арифметика: сложение/вычитание
        when(messageService.getText(anyString(), any(), any(), any())).thenAnswer(inv -> {
            Object a = inv.getArgument(1);
            Object b = inv.getArgument(2);
            Object blank = inv.getArgument(3);
            String key = inv.getArgument(0, String.class);
            if (key != null && key.contains("subtraction")) {
                return a + " - " + b + " = " + blank;
            }
            if (key != null && (key.contains("composition") || key.contains("comparison"))) {
                return a + " = " + b + " + " + blank;
            }
            return a + " + " + b + " = " + blank;
        });
        // Состав числа: оба формата (hide first / hide second). Все аргументы — матчеры.
        when(messageService.getText(eq(GeneratorMessageKeys.FORMAT_COMPOSITION_HIDE_FIRST), any(), any(), any()))
                .thenAnswer(inv -> inv.getArgument(1) + " = " + inv.getArgument(2) + " + " + inv.getArgument(3));
        when(messageService.getText(eq(GeneratorMessageKeys.FORMAT_COMPOSITION_HIDE_SECOND), any(), any(), any()))
                .thenAnswer(inv -> inv.getArgument(1) + " = " + inv.getArgument(2) + " + " + inv.getArgument(3));
        // Сравнение: getText(key, a, blank, b)
        when(messageService.getText(eq(GeneratorMessageKeys.FORMAT_COMPARISON), any(), any(), any()))
                .thenAnswer(inv -> inv.getArgument(1) + " " + inv.getArgument(2) + " " + inv.getArgument(3));
    }

    private static ArithmeticContext contextFor(OperationType type, int quantity) {
        Range range = defaultRange(type);
        Boolean noCarry = defaultNoCarry(type);
        return ArithmeticContext.builder()
                .operationType(type)
                .numberRange(range)
                .quantity(quantity)
                .noCarry(noCarry)
                .build();
    }

    private static Range defaultRange(OperationType type) {
        return switch (type) {
            case ADDITION_10, SUBTRACTION_10 -> new Range(0, 10);
            case ADDITION_20_NO_CARRY, SUBTRACTION_20_NO_CARRY -> new Range(0, 20);
            case NUMBER_COMPOSITION_2_9 -> new Range(2, 9);
            case NUMBER_COMPOSITION_10 -> new Range(10, 10);
            case NUMBER_COMPOSITION_11_20 -> new Range(11, 20);
            case NUMBER_COMPOSITION -> new Range(2, 10);
            case COMPARISON, NUMBER_SEQUENCE -> new Range(0, 20);
        };
    }

    private static Boolean defaultNoCarry(OperationType type) {
        return switch (type) {
            case ADDITION_20_NO_CARRY, SUBTRACTION_20_NO_CARRY -> Boolean.TRUE;
            default -> null;
        };
    }

    static Stream<Arguments> allGeneratorsWithQuantities() {
        return Stream.of(OperationType.values())
                .flatMap(type -> Stream.of(10, 20, 50)
                        .map(q -> Arguments.of(type, q)));
    }

    @ParameterizedTest(name = "{0}: запрос {1} заданий → ровно {1} в списке")
    @MethodSource("allGeneratorsWithQuantities")
    @DisplayName("каждый генератор возвращает ровно запрошенное количество заданий")
    void eachGeneratorReturnsRequestedQuantity(OperationType type, int requestedQuantity) {
        ArithmeticContext context = contextFor(type, requestedQuantity);
        TaskGenerator generator = factory.getGenerator(type);

        List<Task> tasks = generator.generate(context);

        assertThat(tasks)
                .as("Генератор %s при запросе %d заданий", type, requestedQuantity)
                .hasSize(requestedQuantity);
    }

    @Test
    @DisplayName("при quantity = 0 все генераторы возвращают пустой список")
    void eachGeneratorReturnsEmptyList_whenQuantityZero() {
        for (OperationType type : OperationType.values()) {
            ArithmeticContext context = contextFor(type, 0);
            TaskGenerator generator = factory.getGenerator(type);

            List<Task> tasks = generator.generate(context);

            assertThat(tasks)
                    .as("Генератор %s при quantity=0", type)
                    .isEmpty();
        }
    }
}
