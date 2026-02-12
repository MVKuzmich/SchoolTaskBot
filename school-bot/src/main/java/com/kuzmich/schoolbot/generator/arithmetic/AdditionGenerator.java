package com.kuzmich.schoolbot.generator.arithmetic;

import java.util.ArrayList;
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
 * Генератор примеров на сложение в пределах заданного максимума (например 10).
 * Использует домен + уровни ослабления + квоты и двухпроходный отбор
 * ({@link ArithmeticGenerationUtils}).
 * Домен: a ∈ [min, max], b ∈ [0, max - a], ответ a + b; вопрос «a + b = __».
 */
@Component
public class AdditionGenerator implements ArithmeticTaskGenerator {

    private final MessageService messageService;

    public AdditionGenerator(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public OperationType getOperationType() {
        return OperationType.ADDITION_10;
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
                            GeneratorMessageKeys.QUESTION_FORMAT_ADDITION,
                            candidate.a(),
                            candidate.b()
                    );
                    return new Task(question, String.valueOf(candidate.answer()));
                }
        );
    }

    /**
     * Перечисляет весь домен допустимых примеров на сложение в пределах max:
     * a ∈ [min, max], b ∈ [0, max - a], ответ a + b.
     */
    private List<ArithmeticGenerationUtils.Candidate> enumerateDomain(Range range) {
        int min = range.min();
        int max = range.max();
        List<ArithmeticGenerationUtils.Candidate> domain = new ArrayList<>();
        for (int a = min; a <= max; a++) {
            int bMax = max - a;
            for (int b = 0; b <= bMax; b++) {
                int answer = a + b;
                String key = a + "+" + b;
                domain.add(new ArithmeticGenerationUtils.Candidate(a, b, answer, key));
            }
        }
        return domain;
    }

    /**
     * Уровни ослабления для сложения: сначала «интересные» примеры без 0 и 1,
     * затем постепенное разрешение нулей и единиц.
     */
    private List<java.util.function.Predicate<ArithmeticGenerationUtils.Candidate>> buildRelaxationLevels() {
        List<java.util.function.Predicate<ArithmeticGenerationUtils.Candidate>> levels =
                new ArrayList<>();

        // L0: a >= 2, b >= 2, answer >= 2
        levels.add(c -> c.a() >= 2 && c.b() >= 2 && c.answer() >= 2);

        // L1: a >= 2, b >= 2, answer >= 1
        levels.add(c -> c.a() >= 2 && c.b() >= 2 && c.answer() >= 1);

        // L2: a >= 2, b >= 1, answer >= 1
        levels.add(c -> c.a() >= 2 && c.b() >= 1 && c.answer() >= 1);

        // L3: a >= 2, answer >= 1 (разрешаем b = 0)
        levels.add(c -> c.a() >= 2 && c.answer() >= 1);

        // L4: любые неотрицательные
        levels.add(c -> c.answer() >= 0);

        return levels;
    }
}
