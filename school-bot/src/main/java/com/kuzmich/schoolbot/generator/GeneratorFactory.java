package com.kuzmich.schoolbot.generator;

import com.kuzmich.schoolbot.core.validation.Validation;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Фабрика генераторов заданий по типу операции (Strategy Pattern).
 * Регистрирует все бины {@link OperationTaskGenerator} (арифметика и числа/счёт).
 */
@Component
public final class GeneratorFactory {

    private final Map<OperationType, TaskGenerator> generators = new EnumMap<>(OperationType.class);

    public GeneratorFactory(List<OperationTaskGenerator> generatorBeans) {
        Validation.requireNonNull(generatorBeans, "generatorBeans");
        for (OperationTaskGenerator g : generatorBeans) {
            generators.put(g.getOperationType(), g);
        }
        // Проверка при старте: все типы операций должны иметь генератор (избегаем ошибки при нажатии «Создать PDF»).
        for (OperationType type : OperationType.values()) {
            if (!generators.containsKey(type)) {
                throw new IllegalStateException(
                        "Генератор для типа " + type + " не зарегистрирован. Проверьте, что все генераторы (арифметика и числа/счёт) — бины Spring.");
            }
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
