package com.kuzmich.schoolbot.generator.numbers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.kuzmich.schoolbot.core.service.MessageService;
import com.kuzmich.schoolbot.core.validation.Validation;
import com.kuzmich.schoolbot.generator.GenerationContext;
import com.kuzmich.schoolbot.generator.OperationTaskGenerator;
import com.kuzmich.schoolbot.generator.Task;
import com.kuzmich.schoolbot.generator.arithmetic.ArithmeticGenerationUtils;
import com.kuzmich.schoolbot.i18n.GeneratorMessageKeys;

/**
 * Базовый генератор заданий «Состав числа» для диапазона [minN, maxN].
 * Домен: для каждого n и разложения n = a + b — два варианта (скрыть первое или второе слагаемое).
 * Уровни ослабления и эвристики — как в {@link ArithmeticGenerationUtils}.
 */
public abstract class AbstractNumberCompositionGenerator implements OperationTaskGenerator {

    private final int minN;
    private final int maxN;
    private final MessageService messageService;

    protected AbstractNumberCompositionGenerator(MessageService messageService, int minN, int maxN) {
        this.messageService = messageService;
        this.minN = minN;
        this.maxN = maxN;
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

        // Для состава числа: ослабленная эвристика (a = n всегда одинаков) и разрешаем повторы до quantity
        return ArithmeticGenerationUtils.generateWithRelaxation(
                domain,
                quantity,
                levels,
                rnd,
                c -> toTask(c, blank),
                true,
                true
        );
    }

    /**
     * Домен: для каждого n в [minN, maxN] и разложения n = a + b — два варианта:
     * скрыть первое слагаемое (n = __ + b) или второе (n = a + __).
     */
    private List<ArithmeticGenerationUtils.Candidate> enumerateDomain() {
        List<ArithmeticGenerationUtils.Candidate> domain = new ArrayList<>();
        for (int n = minN; n <= maxN; n++) {
            for (int first = 0; first <= n; first++) {
                int second = n - first;
                domain.add(new ArithmeticGenerationUtils.Candidate(n, second, first, n + "," + second + ",F"));
                domain.add(new ArithmeticGenerationUtils.Candidate(n, first, second, n + "," + first + ",S"));
            }
        }
        return domain;
    }

    /**
     * Уровни ослабления: сначала «интересные» примеры без нулей, затем разрешаем 0.
     */
    private List<java.util.function.Predicate<ArithmeticGenerationUtils.Candidate>> buildRelaxationLevels() {
        List<java.util.function.Predicate<ArithmeticGenerationUtils.Candidate>> levels = new ArrayList<>();
        int low = minN;
        // L0: n >= min(low+1, maxN), оба слагаемых >= 1
        levels.add(c -> c.a() >= Math.min(low + 1, maxN) && c.b() >= 1 && c.answer() >= 1);
        // L1: n >= low, оба >= 1
        levels.add(c -> c.a() >= low && c.b() >= 1 && c.answer() >= 1);
        // L2: n >= low, скрытое >= 1 (видимое может быть 0)
        levels.add(c -> c.a() >= low && c.answer() >= 1);
        // L3: любые допустимые
        levels.add(c -> c.a() >= minN && c.answer() >= 0);
        return levels;
    }

    private Task toTask(ArithmeticGenerationUtils.Candidate c, String blank) {
        int n = c.a();
        int visible = c.b();
        int hidden = c.answer();
        String question = c.key().endsWith(",F")
                ? messageService.getText(GeneratorMessageKeys.FORMAT_COMPOSITION_HIDE_FIRST, n, blank, visible)
                : messageService.getText(GeneratorMessageKeys.FORMAT_COMPOSITION_HIDE_SECOND, n, visible, blank);
        return new Task(question, String.valueOf(hidden));
    }
}
