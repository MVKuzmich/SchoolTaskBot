package com.kuzmich.schoolbot.generator.numbers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

import com.kuzmich.schoolbot.core.service.MessageService;
import com.kuzmich.schoolbot.core.validation.Validation;
import com.kuzmich.schoolbot.generator.GenerationContext;
import com.kuzmich.schoolbot.generator.OperationType;
import com.kuzmich.schoolbot.generator.OperationTaskGenerator;
import com.kuzmich.schoolbot.generator.Task;
import com.kuzmich.schoolbot.generator.arithmetic.ArithmeticGenerationUtils;
import com.kuzmich.schoolbot.i18n.GeneratorMessageKeys;

/**
 * Генератор заданий «Сравнение чисел» (знаки &lt;, &gt;, =).
 * Использует домен + уровни ослабления + квоты и эвристики последовательности
 * ({@link ArithmeticGenerationUtils}).
 * Формат вопроса и плейсхолдер задаются в messages (динамические правки).
 */
@Component
public class ComparisonGenerator implements OperationTaskGenerator {

    private final MessageService messageService;

    public ComparisonGenerator(MessageService messageService) {
        this.messageService = messageService;
    }

    private static final int MIN = 0;
    private static final int MAX = 20;

    private static final int SIGN_LT = -1;
    private static final int SIGN_EQ = 0;
    private static final int SIGN_GT = 1;

    @Override
    public OperationType getOperationType() {
        return OperationType.COMPARISON;
    }

    @Override
    public List<Task> generate(GenerationContext context) {
        Validation.requireNonNull(context, "context");
        context.validate();

        int quantity = context.getQuantity();
        if (quantity <= 0) {
            return List.of();
        }

        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        List<ArithmeticGenerationUtils.Candidate> domain = enumerateDomain();
        List<java.util.function.Predicate<ArithmeticGenerationUtils.Candidate>> levels = buildRelaxationLevels();
        String blank = messageService.getText(GeneratorMessageKeys.QUESTION_BLANK);

        return ArithmeticGenerationUtils.generateWithRelaxation(
                domain,
                quantity,
                levels,
                rnd,
                c -> toTask(c, blank)
        );
    }

    /**
     * Домен: все пары (a, b) из [0..20]×[0..20]. answer = -1 (&lt;), 0 (=), 1 (&gt;).
     */
    private List<ArithmeticGenerationUtils.Candidate> enumerateDomain() {
        List<ArithmeticGenerationUtils.Candidate> domain = new ArrayList<>();
        for (int a = MIN; a <= MAX; a++) {
            for (int b = MIN; b <= MAX; b++) {
                int sign = a < b ? SIGN_LT : (a > b ? SIGN_GT : SIGN_EQ);
                domain.add(new ArithmeticGenerationUtils.Candidate(a, b, sign, a + "," + b));
            }
        }
        return domain;
    }

    /**
     * Уровни ослабления: сначала только неравенства (интереснее), затем разрешаем равенство.
     */
    private List<java.util.function.Predicate<ArithmeticGenerationUtils.Candidate>> buildRelaxationLevels() {
        List<java.util.function.Predicate<ArithmeticGenerationUtils.Candidate>> levels = new ArrayList<>();
        // L0: только неравенства (a != b), оба числа >= 2
        levels.add(c -> c.a() != c.b() && c.a() >= 2 && c.b() >= 2);
        // L1: неравенства, разрешаем 0 и 1
        levels.add(c -> c.a() != c.b());
        // L2: разрешаем равенство
        levels.add(c -> true);
        return levels;
    }

    private Task toTask(ArithmeticGenerationUtils.Candidate c, String blank) {
        String signStr = c.answer() == SIGN_LT ? "<" : (c.answer() == SIGN_EQ ? "=" : ">");
        String question = messageService.getText(GeneratorMessageKeys.FORMAT_COMPARISON, c.a(), blank, c.b());
        return new Task(question, signStr);
    }
}
