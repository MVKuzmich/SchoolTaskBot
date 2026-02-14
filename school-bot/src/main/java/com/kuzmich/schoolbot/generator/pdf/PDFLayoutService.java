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
 * Использует переданный шрифт и динамическую раскладку по количеству заданий (сетка, кегль и интервалы подбираются автоматически).
 */
@Service
@Slf4j
public class PDFLayoutService {

    private static final float HEADER_LINE_OFFSET = 20f;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public void addTasksPage(PDDocument document,
                             PDFont font,
                             List<Task> tasks,
                             String title,
                             LocalDate date) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();
        int total = tasks != null ? tasks.size() : 0;
        PdfLayoutParams layout = PdfLayoutParams.forTaskCount(total > 0 ? total : 1, pageWidth, pageHeight);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            // Заголовок
            showSafeText(contentStream, font, layout.titleFontSize(),
                    layout.marginLeft(), pageHeight - layout.marginTop(), title);
            showSafeText(contentStream, font, layout.titleFontSize() * 0.85f,
                    layout.marginLeft(), pageHeight - layout.marginTop() - HEADER_LINE_OFFSET,
                    "Дата: " + date.format(DATE_FORMATTER));

            if (total == 0) {
                return;
            }

            int itemsPerColumn = layout.rows();
            for (int i = 0; i < total; i++) {
                Task task = tasks.get(i);
                int column = i / itemsPerColumn;
                int row = i % itemsPerColumn;

                float x = layout.columnX(column);
                float y = layout.rowY(row);

                showSafeText(contentStream, font, layout.taskFontSize(), x, y, task.question());
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

        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();
        int total = tasks != null ? tasks.size() : 0;
        PdfLayoutParams layout = PdfLayoutParams.forTaskCount(total > 0 ? total : 1, pageWidth, pageHeight);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            showSafeText(contentStream, font, layout.titleFontSize(),
                    layout.marginLeft(), pageHeight - layout.marginTop(), "Ответы");

            if (total == 0) {
                return;
            }

            int itemsPerColumn = layout.rows();
            float answersStartY = pageHeight - layout.marginTop() - HEADER_LINE_OFFSET;

            for (int i = 0; i < total; i++) {
                Task task = tasks.get(i);
                int index = i + 1;
                int column = i / itemsPerColumn;
                int row = i % itemsPerColumn;

                float x = layout.columnX(column);
                float y = answersStartY - row * layout.lineHeight();

                String line = index + ") " + task.answer();
                showSafeText(contentStream, font, layout.taskFontSize(), x, y, line);
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

