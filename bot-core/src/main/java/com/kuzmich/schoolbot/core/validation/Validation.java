package com.kuzmich.schoolbot.core.validation;

/**
 * Единая точка валидации аргументов и входных данных (см. docs/standards/validation.md).
 * Все методы при успехе возвращают переданное значение (для чейнинга), при ошибке выбрасывают {@link ValidationException}.
 */
public final class Validation {

    private Validation() {
    }

    /**
     * Проверяет, что значение не null.
     *
     * @param value    проверяемое значение
     * @param fieldName имя поля (для сообщения об ошибке)
     * @return value при успехе
     * @throws ValidationException если value == null
     */
    public static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " не может быть null");
        }
        return value;
    }

    /**
     * Проверяет, что строка не null, не пустая и не состоит только из пробелов.
     *
     * @param value    проверяемая строка
     * @param fieldName имя поля (для сообщения об ошибке)
     * @return value при успехе
     * @throws ValidationException если value == null или value.isBlank()
     */
    public static String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " не может быть null или пустым");
        }
        return value;
    }

    /**
     * Проверяет, что целое число строго больше 0.
     *
     * @param value    проверяемое значение
     * @param fieldName имя поля (для сообщения об ошибке)
     * @return value при успехе
     * @throws ValidationException если value <= 0
     */
    public static int requirePositiveInt(int value, String fieldName) {
        if (value <= 0) {
            throw new ValidationException(fieldName + " должно быть положительным, получено: " + value);
        }
        return value;
    }

    /**
     * Проверяет, что целое число в диапазоне [min, max] включительно.
     *
     * @param value    проверяемое значение
     * @param min      минимальное допустимое значение
     * @param max      максимальное допустимое значение
     * @param fieldName имя поля (для сообщения об ошибке)
     * @return value при успехе
     * @throws ValidationException если value < min или value > max
     */
    public static int requireInRange(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            throw new ValidationException(
                    fieldName + " должно быть в диапазоне от " + min + " до " + max + ", получено: " + value);
        }
        return value;
    }

    /**
     * Проверяет, что длинное целое строго больше 0 (например, для userId, chatId).
     *
     * @param value    проверяемое значение
     * @param fieldName имя поля (для сообщения об ошибке)
     * @return value при успехе
     * @throws ValidationException если value == null или value <= 0
     */
    public static long requirePositiveLong(Long value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " не может быть null");
        }
        if (value <= 0) {
            throw new ValidationException(fieldName + " должно быть положительным, получено: " + value);
        }
        return value;
    }

    /**
     * Проверяет, что строка не null, не пустая и входит в множество допустимых значений.
     * Используется для валидации команд (ручной ввод) и callback_data (нажатие кнопки).
     *
     * @param value     проверяемая строка (обычно уже trim при вызове)
     * @param fieldName имя поля (для сообщения об ошибке)
     * @param allowed   допустимые значения (хотя бы одно)
     * @return value при успехе
     * @throws ValidationException если value == null, value.isBlank() или value не входит в allowed
     */
    public static String requireOneOf(String value, String fieldName, String... allowed) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " не может быть null или пустым");
        }
        String trimmed = value.trim();
        for (String a : allowed) {
            if (a != null && a.equals(trimmed)) {
                return trimmed;
            }
        }
        throw new ValidationException(
                fieldName + " должно быть одним из допустимых значений, получено: " + trimmed);
    }

    /**
     * Проверяет, что строка не null, не пустая и начинается с заданного префикса.
     * Используется для slash-команд, когда допустимы варианты вроде /start и /start@BotName.
     *
     * @param value     проверяемая строка
     * @param prefix    ожидаемое начало строки (например, "/start")
     * @param fieldName имя поля (для сообщения об ошибке)
     * @return value.trim() при успехе
     * @throws ValidationException если value == null, value.isBlank() или не начинается с prefix
     */
    public static String requireStartsWith(String value, String prefix, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " не может быть null или пустым");
        }
        String trimmed = value.trim();
        if (!trimmed.startsWith(prefix)) {
            throw new ValidationException(
                    fieldName + " должно начинаться с \"" + prefix + "\", получено: " + trimmed);
        }
        return trimmed;
    }
}
