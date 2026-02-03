package com.kuzmich.schoolbot.core.service;

import java.util.Optional;

/**
 * Сервис контекста пользователя — «что выбрал пользователь».
 * Конкретный тип контекста (режим, класс, предмет, тема и т.д.) задаётся реализацией в модуле бота (school-bot).
 */
public interface UserContextService<T> {

    /**
     * Получить контекст пользователя. Если контекста нет — создаётся новый.
     */
    T getOrCreate(Long userId);

    /**
     * Получить контекст пользователя, если он есть.
     */
    Optional<T> get(Long userId);

    /**
     * Сохранить контекст пользователя.
     */
    void save(T context);

    /**
     * Сбросить контекст пользователя (например, при возврате в главное меню).
     */
    void clear(Long userId);
}
