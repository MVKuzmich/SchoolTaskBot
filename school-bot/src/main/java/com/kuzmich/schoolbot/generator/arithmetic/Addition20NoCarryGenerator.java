package com.kuzmich.schoolbot.generator.arithmetic;

import com.kuzmich.schoolbot.core.service.MessageService;
import com.kuzmich.schoolbot.core.validation.Validation;
import com.kuzmich.schoolbot.generator.ArithmeticContext;
import com.kuzmich.schoolbot.generator.GenerationContext;
import com.kuzmich.schoolbot.generator.ArithmeticTaskGenerator;
import com.kuzmich.schoolbot.generator.OperationType;
import com.kuzmich.schoolbot.generator.Task;
import com.kuzmich.schoolbot.generator.TaskGenerator;
import com.kuzmich.schoolbot.i18n.GeneratorMessageKeys;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Генератор примеров на сложение в пределах 20 без перехода через десяток.
 * Алгоритм: десятки 0 или 10, единицы a + единицы b &lt; 10; результат ≤ 20.
 */
@Component
public class Addition20NoCarryGenerator implements ArithmeticTaskGenerator {

    private final MessageService messageService;

    public Addition20NoCarryGenerator(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public OperationType getOperationType() {
        return OperationType.ADDITION_20_NO_CARRY;
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

        List<Task> tasks = new ArrayList<>(quantity);
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        for (int i = 0; i < quantity; i++) {
            int d = rnd.nextBoolean() ? 0 : 10;
            int aUnits = rnd.nextInt(10);
            int bUnitsMax = 9 - aUnits;
            int bUnits = bUnitsMax >= 0 ? rnd.nextInt(bUnitsMax + 1) : 0;
            int a = d + aUnits;
            int b = bUnits;
            int answer = a + b;
            String question = messageService.getText(GeneratorMessageKeys.QUESTION_FORMAT_ADDITION, a, b);
            tasks.add(new Task(question, String.valueOf(answer)));
        }

        return tasks;
    }
}
