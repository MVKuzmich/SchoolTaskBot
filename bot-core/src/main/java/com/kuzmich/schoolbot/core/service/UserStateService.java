package com.kuzmich.schoolbot.core.service;

/**
 * Сервис состояния пользователя для интерактивных диалогов (wizard настройки, ввод текста и т.д.).
 * Конкретная реализация и enum состояний определяются в модуле бота (school-bot).
 */
public interface UserStateService {

    /**
     * Текущее состояние пользователя. Тип состояния — на усмотрение реализации.
     */
    Object getState(Long userId);

    /**
     * Установить состояние пользователя.
     */
    void setState(Long userId, Object state);

    /**
     * Сбросить состояние (например, после завершения wizard или отмены).
     */
    void clearState(Long userId);

    /**
     * Проверка: ожидает ли бот от пользователя ввод (текст/кнопку) в рамках диалога.
     */
    boolean isWaitingForInput(Long userId);
}
