package com.kuzmich.schoolbot.generator.arithmetic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

import com.kuzmich.schoolbot.core.service.MessageService;
import com.kuzmich.schoolbot.core.validation.Validation;
import com.kuzmich.schoolbot.generator.ArithmeticContext;
import com.kuzmich.schoolbot.i18n.GeneratorMessageKeys;
import com.kuzmich.schoolbot.generator.ArithmeticTaskGenerator;
import com.kuzmich.schoolbot.generator.GenerationContext;
import com.kuzmich.schoolbot.generator.OperationType;
import com.kuzmich.schoolbot.generator.Range;
import com.kuzmich.schoolbot.generator.Task;

/**
 * Генератор примеров на сложение в пределах заданного максимума (например 10).
 * Алгоритм: a ∈ [min, max], b ∈ [0, max - a], ответ a + b; вопрос в формате «a + b = __».
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
        int min = range.min();
        int max = range.max();
        boolean requireUnique = quantity <= 50 && min == 0 && max == 10;

        if (requireUnique) {
            return generateUnique(quantity, min, max);
        }

        List<Task> tasks = new ArrayList<>(quantity);
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 0; i < quantity; i++) {
            tasks.add(generateOneTask(min, max, rnd));
        }
        return tasks;
    }

    private List<Task> generateUnique(int quantity, int min, int max) {
        Set<String> used = new HashSet<>();
        List<Task> tasks = new ArrayList<>(quantity);
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        int maxAttempts = quantity * 50;
        int attempts = 0;
        while (tasks.size() < quantity && attempts < maxAttempts) {
            Task task = generateOneTask(min, max, rnd);
            if (used.add(task.question())) {
                tasks.add(task);
            }
            attempts++;
        }
        return tasks;
    }

    private Task generateOneTask(int min, int max, ThreadLocalRandom rnd) {
        int a = min + rnd.nextInt(max - min + 1);
        int bMax = max - a;
        int b = bMax >= 0 ? rnd.nextInt(bMax + 1) : 0;
        int answer = a + b;
        String question = messageService.getText(GeneratorMessageKeys.QUESTION_FORMAT_ADDITION, a, b);
        return new Task(question, String.valueOf(answer));
    }
}
