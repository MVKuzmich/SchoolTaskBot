package com.kuzmich.schoolbot.generator.pdf;

import com.kuzmich.schoolbot.generator.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

/**
 * Сервис верхнего уровня: из списка Task формирует PDF (страница с заданиями + страница с ответами).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PDFService {

    private static final String FONT_PATH = "/fonts/DejaVuSans.ttf";

    private final PDFLayoutService layoutService;

    public byte[] generate(List<Task> tasks, String title) {
        try (PDDocument document = new PDDocument()) {
            PDFont font = loadFont(document);

            layoutService.addTasksPage(document, font, tasks, title, LocalDate.now());
            layoutService.addAnswersPage(document, font, tasks);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);

            byte[] bytes = out.toByteArray();
            log.info("PDF generated successfully, size: {} bytes, tasks: {}", bytes.length,
                    tasks != null ? tasks.size() : 0);
            return bytes;
        } catch (IOException e) {
            log.error("Failed to generate PDF: {}", e.getMessage(), e);
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    private PDFont loadFont(PDDocument document) throws IOException {
        InputStream fontStream = PDFService.class.getResourceAsStream(FONT_PATH);
        if (fontStream != null) {
            try (InputStream is = fontStream) {
                return PDType0Font.load(document, is);
            } catch (IOException e) {
                log.warn("Failed to load DejaVuSans font from classpath {}, falling back to built-in font", FONT_PATH, e);
            }
        } else {
            log.warn("Font resource {} not found on classpath, falling back to built-in font", FONT_PATH);
        }
        // Fallback: базовый шрифт без гарантированной поддержки кириллицы,
        // чтобы генерация не падала в окружении без DejaVuSans.ttf.
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    }
}

