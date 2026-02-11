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
        int min = range.min();
        int max = range.max();
        List<Task> tasks = new ArrayList<>(quantity);
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        for (int i = 0; i < quantity; i++) {
            int a = min + rnd.nextInt(max - min + 1);
            int b = rnd.nextInt(a + 1);
            int answer = a - b;
            String question = messageService.getText(GeneratorMessageKeys.QUESTION_FORMAT_SUBTRACTION, a, b);
            tasks.add(new Task(question, String.valueOf(answer)));
        }

        return tasks;
    }
}
