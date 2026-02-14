package com.kuzmich.schoolbot.generator.numbers;

import com.kuzmich.schoolbot.core.service.MessageService;
import com.kuzmich.schoolbot.generator.OperationType;
import org.springframework.stereotype.Component;

/** Генератор заданий «Состав числа» только для чисел 2–9. */
@Component
public class NumberComposition2To9Generator extends AbstractNumberCompositionGenerator {

    public NumberComposition2To9Generator(MessageService messageService) {
        super(messageService, 2, 9);
    }

    @Override
    public OperationType getOperationType() {
        return OperationType.NUMBER_COMPOSITION_2_9;
    }
}
