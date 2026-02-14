package com.kuzmich.schoolbot.generator.numbers;

import com.kuzmich.schoolbot.core.service.MessageService;
import com.kuzmich.schoolbot.generator.OperationType;
import org.springframework.stereotype.Component;

/** Генератор заданий «Состав числа» только для числа 10 (разложение десятки). */
@Component
public class NumberComposition10Generator extends AbstractNumberCompositionGenerator {

    public NumberComposition10Generator(MessageService messageService) {
        super(messageService, 10, 10);
    }

    @Override
    public OperationType getOperationType() {
        return OperationType.NUMBER_COMPOSITION_10;
    }
}
