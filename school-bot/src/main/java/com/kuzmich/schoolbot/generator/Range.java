package com.kuzmich.schoolbot.generator;

/**
 * Диапазон целых чисел (min, max включительно) для операндов или результата генерации.
 */
public record Range(int min, int max) {

    public Range {
        if (min > max) {
            throw new IllegalArgumentException("min не может быть больше max: min=" + min + ", max=" + max);
        }
    }
}
