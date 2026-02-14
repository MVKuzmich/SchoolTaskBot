package com.kuzmich.schoolbot.generator.numbers;

import com.kuzmich.schoolbot.core.service.MessageService;
import com.kuzmich.schoolbot.generator.OperationType;
import org.springframework.stereotype.Component;

/** Генератор заданий «Состав числа» для чисел 11–20. */
@Component
public class NumberComposition11To20Generator extends AbstractNumberCompositionGenerator {

    public NumberComposition11To20Generator(MessageService messageService) {
        super(messageService, 11, 20);
    }

    @Override
    public OperationType getOperationType() {
        return OperationType.NUMBER_COMPOSITION_11_20;
    }
}
