package com.kuzmich.schoolbot.generator.pdf;

import com.kuzmich.schoolbot.generator.Task;
import com.kuzmich.schoolbot.generator.service.PdfGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Сервис верхнего уровня: из списка Task формирует PDF (страница с заданиями + страница с ответами).
 * Шрифт задаётся через {@code pdf.font.path} (файл или classpath), без хардкода — можно менять на лету.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PDFService {

    private static final String CLASSPATH_PREFIX = "classpath:";
    /** Префикс для classpath-ресурсов (в Java всегда «/», не системный разделитель). */
    private static final String RESOURCE_PATH_PREFIX = "/";
    /** Запасной путь к шрифту (из зависимости jasperreports-fonts), если в resources/fonts/ файла нет. */
    private static final String FALLBACK_FONT_PATH = "classpath:/net/sf/jasperreports/fonts/dejavu/DejaVuSans.ttf";

    @Value("${pdf.font.path:classpath:/fonts/DejaVuSans.ttf}")
    private String fontPath;

    private final PDFLayoutService layoutService;

    public byte[] generate(List<Task> tasks, String title) {
        try (PDDocument document = new PDDocument()) {
            PDFont font = loadFont(document);

            layoutService.addTasksPage(document, font, tasks, title, LocalDate.now());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);

            byte[] bytes = out.toByteArray();
            log.info("PDF generated successfully, size: {} bytes, tasks: {}", bytes.length,
                    tasks != null ? tasks.size() : 0);
            return bytes;
        } catch (IOException e) {
            log.error("Failed to generate PDF: {}", e.getMessage(), e);
            throw new PdfGenerationException("PDF generation failed", e);
        }
    }

    private PDFont loadFont(PDDocument document) throws IOException {
        String path = fontPath != null ? fontPath.trim() : "";
        if (path.isEmpty()) {
            log.warn("pdf.font.path пуст — используется встроенный шрифт Helvetica (кириллица отображаться не будет)");
            return fallbackFont();
        }

        InputStream fontStream = openFontStream(path);
        if (fontStream == null && path.equals("classpath:/fonts/DejaVuSans.ttf")) {
            fontStream = openFontStream(FALLBACK_FONT_PATH);
            if (fontStream != null) {
                path = FALLBACK_FONT_PATH;
                log.debug("Шрифт из resources/fonts/ не найден, используется из jasperreports-fonts");
            }
        }
        if (fontStream != null) {
            try (InputStream is = fontStream) {
                PDFont font = PDType0Font.load(document, is);
                log.info("Шрифт для PDF загружен: {} (кириллица поддерживается)", path);
                return font;
            } catch (IOException e) {
                log.warn("Не удалось загрузить шрифт из {}: {} — используется Helvetica (кириллица не будет)", path, e.getMessage());
            }
        } else {
            log.warn("Шрифт не найден по пути: {} — используется Helvetica. Добавьте TTF в resources/fonts/ или укажите pdf.font.path", path);
        }
        return fallbackFont();
    }

    /**
     * Открывает поток шрифта: из classpath (classpath:/...) или с диска (абсолютный/относительный путь).
     */
    private InputStream openFontStream(String path) throws IOException {
        if (path.startsWith(CLASSPATH_PREFIX)) {
            String resourcePath = path.substring(CLASSPATH_PREFIX.length()).stripLeading();
            if (!resourcePath.startsWith(RESOURCE_PATH_PREFIX)) {
                resourcePath = RESOURCE_PATH_PREFIX + resourcePath;
            }
            return getClass().getResourceAsStream(resourcePath);
        }
        // Путь к файлу на диске
        Path filePath = Path.of(path);
        if (Files.isRegularFile(filePath) && Files.isReadable(filePath)) {
            return new FileInputStream(filePath.toFile());
        }
        return null;
    }

    private static PDFont fallbackFont() {
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    }
}

