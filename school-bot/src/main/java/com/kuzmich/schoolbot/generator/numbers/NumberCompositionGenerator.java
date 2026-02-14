package com.kuzmich.schoolbot.generator.numbers;

import com.kuzmich.schoolbot.core.service.MessageService;
import com.kuzmich.schoolbot.generator.OperationType;
import org.springframework.stereotype.Component;

/**
 * Генератор заданий «Состав числа» смешанный (числа 2–10).
 * Переиспользует логику {@link AbstractNumberCompositionGenerator}.
 */
@Component
public class NumberCompositionGenerator extends AbstractNumberCompositionGenerator {

    public NumberCompositionGenerator(MessageService messageService) {
        super(messageService, 2, 10);
    }

    @Override
    public OperationType getOperationType() {
        return OperationType.NUMBER_COMPOSITION;
    }
}
