package com.kuzmich.schoolbot.generator.pdf;

import com.kuzmich.schoolbot.generator.Task;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PDFServiceTest {

    private final PDFLayoutService layoutService = new PDFLayoutService();
    private final PDFService pdfService = new PDFService(layoutService);

    @Test
    void shouldGeneratePDF_withTitleAndSingleTasksPage() throws IOException {
        // given
        List<Task> tasks = List.of(
                new Task("3 + 4 = ", "7", Map.of()),
                new Task("5 + 2 = ", "7", Map.of())
        );
        // В этом тесте используем ASCII-заголовок, чтобы не зависеть от наличия кириллического шрифта.
        String title = "Arithmetic: Addition";

        // when
        byte[] pdf = pdfService.generate(tasks, title);

        // then
        assertThat(pdf).isNotEmpty();

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            // Одна страница с заданиями (страница ответов не генерируется)
            assertThat(doc.getNumberOfPages()).isEqualTo(1);
        }
    }
}

