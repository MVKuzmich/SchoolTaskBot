package com.kuzmich.schoolbot.generator.arithmetic;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

import com.kuzmich.schoolbot.core.service.MessageService;
import com.kuzmich.schoolbot.core.validation.Validation;
import com.kuzmich.schoolbot.generator.ArithmeticContext;
import com.kuzmich.schoolbot.generator.ArithmeticTaskGenerator;
import com.kuzmich.schoolbot.generator.GenerationContext;
import com.kuzmich.schoolbot.generator.OperationType;
import com.kuzmich.schoolbot.generator.Range;
import com.kuzmich.schoolbot.generator.Task;
import com.kuzmich.schoolbot.i18n.GeneratorMessageKeys;

/**
 * Генератор примеров на вычитание в пределах заданного максимума (например 10).
 * Алгоритм: a ∈ [min, max], b ∈ [0, a], ответ a - b (неотрицательный); вопрос «a - b = __».
 */
@Component
public class SubtractionGenerator implements ArithmeticTaskGenerator {

    private final MessageService messageService;

    public SubtractionGenerator(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public OperationType getOperationType() {
        return OperationType.SUBTRACTION_10;
    }

    @Override
    public List<Task> generate(GenerationContext context) {
        Validation.requireNonNull(context, "context");
        ArithmeticContext ctx = (ArithmeticContext) context;
        ctx.validate();
        int quantity = ctx.getQuantity();
        if (quantity == 0) {
            return List.of();
        }

        Range range = ctx.getNumberRange();
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        List<ArithmeticGenerationUtils.Candidate> domain = enumerateDomain(range);
        List<java.util.function.Predicate<ArithmeticGenerationUtils.Candidate>> levels =
                buildRelaxationLevels();

        return ArithmeticGenerationUtils.generateWithRelaxation(
                domain,
                quantity,
                levels,
                rnd,
                candidate -> {
                    String question = messageService.getText(
                            GeneratorMessageKeys.QUESTION_FORMAT_SUBTRACTION,
                            candidate.a(),
                            candidate.b()
                    );
                    return new Task(question, String.valueOf(candidate.answer()));
                }
        );
    }

    /**
     * Перечисляет весь домен допустимых примеров на вычитание:
     * a ∈ [min, max], b ∈ [0, a], ответ a - b (неотрицательный).
     */
    private List<ArithmeticGenerationUtils.Candidate> enumerateDomain(Range range) {
        int min = range.min();
        int max = range.max();
        List<ArithmeticGenerationUtils.Candidate> domain = new java.util.ArrayList<>();
        for (int a = min; a <= max; a++) {
            for (int b = 0; b <= a; b++) {
                int answer = a - b;
                String key = a + "-" + b;
                domain.add(new ArithmeticGenerationUtils.Candidate(a, b, answer, key));
            }
        }
        return domain;
    }

    /**
     * Описывает уровни "ослабления" ограничений для вычитания.
     * <p>
     * Идея:
     * <ul>
     *     <li>L0 — максимально "интересные" примеры без нулей и единиц в операндах и ответе.</li>
     *     <li>L1 — разрешаем ответ 1, но операнды остаются ≥ 2.</li>
     *     <li>L2 — разрешаем b = 1, но запрещаем ответ 0.</li>
     *     <li>L3 — разрешаем b = 0, но по возможности избегаем ответа 0.</li>
     *     <li>L4 — самый мягкий уровень: любые неотрицательные ответы.</li>
     * </ul>
     * Такой порядок даёт контролируемое "размягчение" качества листа, если строгих примеров не хватает.
     */
    private List<java.util.function.Predicate<ArithmeticGenerationUtils.Candidate>> buildRelaxationLevels() {
        java.util.List<java.util.function.Predicate<ArithmeticGenerationUtils.Candidate>> levels =
                new java.util.ArrayList<>();

        // L0: a >= 2, b >= 2, answer >= 2
        levels.add(c -> c.a() >= 2 && c.b() >= 2 && c.answer() >= 2);

        // L1: a >= 2, b >= 2, answer >= 1 (разрешаем ответ 1)
        levels.add(c -> c.a() >= 2 && c.b() >= 2 && c.answer() >= 1);

        // L2: a >= 2, b >= 1, answer >= 1 (разрешаем b = 1)
        levels.add(c -> c.a() >= 2 && c.b() >= 1 && c.answer() >= 1);

        // L3: a >= 2, answer >= 1 (разрешаем b = 0, но избегаем ответа 0)
        levels.add(c -> c.a() >= 2 && c.answer() >= 1);

        // L4: самый мягкий уровень — любые неотрицательные ответы
        levels.add(c -> c.answer() >= 0);

        return levels;
    }
}
