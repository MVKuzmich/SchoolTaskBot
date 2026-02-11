package com.kuzmich.schoolbot.generator;

import java.util.Collections;
import java.util.Map;

/**
 * Одно задание: текст вопроса, ответ и опциональные метаданные.
 */
public record Task(String question, String answer, Map<String, Object> metadata) {

    /**
     * Создаёт задание с пустыми метаданными.
     */
    public Task(String question, String answer) {
        this(question, answer, Collections.emptyMap());
    }
}
