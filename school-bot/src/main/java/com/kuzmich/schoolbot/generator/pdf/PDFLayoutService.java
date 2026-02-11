package com.kuzmich.schoolbot.generator.pdf;

import com.kuzmich.schoolbot.generator.Task;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Отвечает за визуальное оформление PDF: страницы с заданиями и с ответами.
 * Использует переданный шрифт и базовые константы отступов.
 */
@Service
@Slf4j
public class PDFLayoutService {

    private static final float MARGIN_LEFT = 50f;
    private static final float MARGIN_TOP = 50f;
    private static final float LINE_HEIGHT = 16f;
    private static final float TITLE_FONT_SIZE = 16f;
    private static final float TEXT_FONT_SIZE = 12f;
    private static final float COLUMN_GAP = 40f;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public void addTasksPage(PDDocument document,
                             PDFont font,
                             List<Task> tasks,
                             String title,
                             LocalDate date) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();

            // Заголовок
            showSafeText(contentStream, font, TITLE_FONT_SIZE,
                    MARGIN_LEFT, pageHeight - MARGIN_TOP, title);
            showSafeText(contentStream, font, TEXT_FONT_SIZE,
                    MARGIN_LEFT, pageHeight - MARGIN_TOP - LINE_HEIGHT,
                    "Дата: " + date.format(DATE_FORMATTER));

            // Задания в две колонки
            int total = tasks != null ? tasks.size() : 0;
            if (total == 0) {
                return;
            }

            int itemsPerColumn = (int) Math.ceil(total / 2.0);
            float contentStartY = pageHeight - MARGIN_TOP - 3 * LINE_HEIGHT;

            float contentWidth = pageWidth - 2 * MARGIN_LEFT;
            float columnWidth = (contentWidth - COLUMN_GAP) / 2;
            float leftColumnX = MARGIN_LEFT;
            float rightColumnX = MARGIN_LEFT + columnWidth + COLUMN_GAP;

            for (int i = 0; i < total; i++) {
                Task task = tasks.get(i);
                int index = i + 1;
                int column = i / itemsPerColumn;
                int row = i % itemsPerColumn;

                float x = (column == 0) ? leftColumnX : rightColumnX;
                float y = contentStartY - row * LINE_HEIGHT;

                String line = index + ") " + task.question();

                showSafeText(contentStream, font, TEXT_FONT_SIZE, x, y, line);
            }
        } catch (IOException e) {
            log.error("Ошибка при рисовании страницы с заданиями", e);
            throw e;
        }
    }

    public void addAnswersPage(PDDocument document,
                               PDFont font,
                               List<Task> tasks) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();

            // Заголовок
            showSafeText(contentStream, font, TITLE_FONT_SIZE,
                    MARGIN_LEFT, pageHeight - MARGIN_TOP, "Ответы");

            int total = tasks != null ? tasks.size() : 0;
            if (total == 0) {
                return;
            }

            int itemsPerColumn = (int) Math.ceil(total / 2.0);
            float contentStartY = pageHeight - MARGIN_TOP - 2 * LINE_HEIGHT;

            float contentWidth = pageWidth - 2 * MARGIN_LEFT;
            float columnWidth = (contentWidth - COLUMN_GAP) / 2;
            float leftColumnX = MARGIN_LEFT;
            float rightColumnX = MARGIN_LEFT + columnWidth + COLUMN_GAP;

            for (int i = 0; i < total; i++) {
                Task task = tasks.get(i);
                int index = i + 1;
                int column = i / itemsPerColumn;
                int row = i % itemsPerColumn;

                float x = (column == 0) ? leftColumnX : rightColumnX;
                float y = contentStartY - row * LINE_HEIGHT;

                String line = index + ") " + task.answer();

                showSafeText(contentStream, font, TEXT_FONT_SIZE, x, y, line);
            }
        } catch (IOException e) {
            log.error("Ошибка при рисовании страницы с ответами", e);
            throw e;
        }
    }

    /**
     * Безопасный вывод текста: если шрифт не поддерживает часть символов (например, кириллицу),
     * IllegalArgumentException перехватывается, и строка деградирует до ASCII-представления,
     * чтобы генерация PDF не падала.
     */
    private void showSafeText(PDPageContentStream contentStream,
                              PDFont font,
                              float fontSize,
                              float x,
                              float y,
                              String text) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        try {
            contentStream.showText(text);
        } catch (IllegalArgumentException e) {
            String fallback = text.replaceAll("[^\\p{ASCII}]", "?");
            log.warn("Текст '{}' содержит символы, недоступные для шрифта {}, используем fallback '{}'",
                    text, font, fallback, e);
            contentStream.showText(fallback);
        } finally {
            contentStream.endText();
        }
    }
}

