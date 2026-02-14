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
 * Генератор заданий «Продолжи числовой ряд».
 * Использует домен + уровни ослабления + квоты и эвристики последовательности
 * ({@link ArithmeticGenerationUtils}).
 * Плейсхолдер пропуска задаётся в messages (generator.question.blank).
 */
@Component
public class NumberSequenceGenerator implements OperationTaskGenerator {

    private final MessageService messageService;

    public NumberSequenceGenerator(MessageService messageService) {
        this.messageService = messageService;
    }

    private static final int START_MIN = 0;
    private static final int START_MAX = 15;
    private static final int SERIES_LENGTH = 5;

    @Override
    public OperationType getOperationType() {
        return OperationType.NUMBER_SEQUENCE;
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
     * Домен: start ∈ [0..15], gapIndex ∈ [0..4]. a = start, b = gapIndex, answer = start + gapIndex.
     */
    private List<ArithmeticGenerationUtils.Candidate> enumerateDomain() {
        List<ArithmeticGenerationUtils.Candidate> domain = new ArrayList<>();
        for (int start = START_MIN; start <= START_MAX; start++) {
            for (int gapIndex = 0; gapIndex < SERIES_LENGTH; gapIndex++) {
                int hidden = start + gapIndex;
                domain.add(new ArithmeticGenerationUtils.Candidate(start, gapIndex, hidden, start + "," + gapIndex));
            }
        }
        return domain;
    }

    /**
     * Уровни ослабления: сначала пропуск в середине ряда и start >= 1, затем разрешаем края.
     */
    private List<java.util.function.Predicate<ArithmeticGenerationUtils.Candidate>> buildRelaxationLevels() {
        List<java.util.function.Predicate<ArithmeticGenerationUtils.Candidate>> levels = new ArrayList<>();
        // L0: start >= 1, пропуск не на краях (индекс 1, 2 или 3)
        levels.add(c -> c.a() >= 1 && c.b() >= 1 && c.b() <= 3);
        // L1: start >= 1, любая позиция пропуска
        levels.add(c -> c.a() >= 1);
        // L2: разрешаем start = 0
        levels.add(c -> true);
        return levels;
    }

    private Task toTask(ArithmeticGenerationUtils.Candidate c, String blank) {
        int start = c.a();
        int gapIndex = c.b();
        int hidden = c.answer();
        StringBuilder question = new StringBuilder();
        for (int j = 0; j < SERIES_LENGTH; j++) {
            if (j > 0) {
                question.append(", ");
            }
            if (j == gapIndex) {
                question.append(blank);
            } else {
                question.append(start + j);
            }
        }
        return new Task(question.toString(), String.valueOf(hidden));
    }
}
