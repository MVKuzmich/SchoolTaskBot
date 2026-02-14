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
import com.kuzmich.schoolbot.generator.Task;
import com.kuzmich.schoolbot.i18n.GeneratorMessageKeys;

/**
 * Генератор примеров на вычитание в пределах 20 без перехода через десяток.
 * Использует домен + уровни ослабления + квоты и двухпроходный отбор
 * ({@link ArithmeticGenerationUtils}).
 * Домен: a ∈ [0, 19], b ∈ [0, a mod 10] (единицы a ≥ единицы b), ответ a − b.
 */
@Component
public class Subtraction20NoCarryGenerator implements ArithmeticTaskGenerator {

    private final MessageService messageService;

    public Subtraction20NoCarryGenerator(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public OperationType getOperationType() {
        return OperationType.SUBTRACTION_20_NO_CARRY;
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

        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        List<ArithmeticGenerationUtils.Candidate> domain = enumerateDomain();
        List<java.util.function.Predicate<ArithmeticGenerationUtils.Candidate>> levels =
                buildRelaxationLevels();
        String blank = messageService.getText(GeneratorMessageKeys.QUESTION_BLANK);

        return ArithmeticGenerationUtils.generateWithRelaxation(
                domain,
                quantity,
                levels,
                rnd,
                candidate -> {
                    String question = messageService.getText(
                            GeneratorMessageKeys.QUESTION_FORMAT_SUBTRACTION,
                            candidate.a(),
                            candidate.b(),
                            blank
                    );
                    return new Task(question, String.valueOf(candidate.answer()));
                }
        );
    }

    /**
     * Перечисляет домен: вычитание в пределах 20 без перехода (без заёма в единицах).
     * a ∈ [0, 19], b ∈ [0, a mod 10], ответ a − b.
     */
    private List<ArithmeticGenerationUtils.Candidate> enumerateDomain() {
        List<ArithmeticGenerationUtils.Candidate> domain = new ArrayList<>();
        for (int a = 0; a <= 19; a++) {
            int bMax = a % 10;
            for (int b = 0; b <= bMax; b++) {
                int answer = a - b;
                String key = a + "-" + b;
                domain.add(new ArithmeticGenerationUtils.Candidate(a, b, answer, key));
            }
        }
        return domain;
    }

    /**
     * Уровни ослабления: приоритет примерам без 0 и 1 в операндах и ответе.
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

        // L4: любые неотрицательные из домена
        levels.add(c -> c.answer() >= 0);

        return levels;
    }
}
