package com.kuzmich.schoolbot.generator;

import com.kuzmich.schoolbot.core.validation.Validation;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Фабрика генераторов заданий по типу операции (Strategy Pattern).
 * Регистрирует все бины {@link ArithmeticTaskGenerator}, переданные в конструктор.
 */
@Component
public final class GeneratorFactory {

    private final Map<OperationType, TaskGenerator> generators = new EnumMap<>(OperationType.class);

    public GeneratorFactory(List<ArithmeticTaskGenerator> generatorBeans) {
        Validation.requireNonNull(generatorBeans, "generatorBeans");
        for (ArithmeticTaskGenerator g : generatorBeans) {
            generators.put(g.getOperationType(), g);
        }
    }

    /**
     * Возвращает генератор для заданного типа операции.
     *
     * @param type тип операции; не null
     * @return соответствующий генератор
     */
    public TaskGenerator getGenerator(OperationType type) {
        Validation.requireNonNull(type, "type");
        TaskGenerator generator = generators.get(type);
        if (generator == null) {
            throw new IllegalArgumentException("Генератор для типа " + type + " не зарегистрирован");
        }
        return generator;
    }
}
