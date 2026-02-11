package com.kuzmich.schoolbot.core.validation;

/**
 * Исключение при нарушении правил валидации (null, пустая строка, неверный диапазон и т.д.).
 * Наследуется от {@link IllegalArgumentException}, чтобы общие обработчики ошибок
 * (см. docs/standards/errors.md) могли единообразно обрабатывать ошибки валидации.
 */
public class ValidationException extends IllegalArgumentException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
