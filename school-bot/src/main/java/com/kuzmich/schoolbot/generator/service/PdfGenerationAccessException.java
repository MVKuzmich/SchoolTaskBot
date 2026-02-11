package com.kuzmich.schoolbot.generator.service;

/**
 * Исключение высокого уровня для отказа в генерации PDF (например, из-за лимитов фичи).
 * Сообщение исключения предполагается показывать пользователю.
 */
public class PdfGenerationAccessException extends RuntimeException {

    public PdfGenerationAccessException(String message) {
        super(message);
    }
}

