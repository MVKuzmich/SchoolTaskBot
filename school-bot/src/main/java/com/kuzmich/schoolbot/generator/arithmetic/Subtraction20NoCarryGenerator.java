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
 * Алгоритм: единицы a >= единицы b; результат неотрицательный.
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

        List<Task> tasks = new ArrayList<>(quantity);
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        for (int i = 0; i < quantity; i++) {
            int d = rnd.nextBoolean() ? 0 : 10;
            int aUnits = rnd.nextInt(10);
            int bUnits = rnd.nextInt(aUnits + 1);
            int a = d + aUnits;
            int b = bUnits;
            int answer = a - b;
            String question = messageService.getText(GeneratorMessageKeys.QUESTION_FORMAT_SUBTRACTION, a, b);
            tasks.add(new Task(question, String.valueOf(answer)));
        }

        return tasks;
    }
}
