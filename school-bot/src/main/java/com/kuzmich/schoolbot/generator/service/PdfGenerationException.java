package com.kuzmich.schoolbot.generator.service;

/**
 * Исключение при техническом сбое генерации PDF (IO, шрифт, разметка).
 * В отличие от {@link PdfGenerationAccessException} — не про лимиты доступа.
 */
public class PdfGenerationException extends RuntimeException {

    public PdfGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
