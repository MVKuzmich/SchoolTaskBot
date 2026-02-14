package com.kuzmich.schoolbot.generator.service;

import com.kuzmich.schoolbot.core.premium.AccessCheckResult;
import com.kuzmich.schoolbot.core.premium.FeatureAccessService;
import com.kuzmich.schoolbot.generator.ArithmeticContext;
import com.kuzmich.schoolbot.generator.GeneratorFactory;
import com.kuzmich.schoolbot.generator.OperationType;
import com.kuzmich.schoolbot.generator.Range;
import com.kuzmich.schoolbot.generator.Task;
import com.kuzmich.schoolbot.generator.TaskGenerator;
import com.kuzmich.schoolbot.generator.pdf.PDFService;
import com.kuzmich.schoolbot.subscription.Feature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Доменный фасад: связывает генераторы заданий, квоты фич и PDFService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGenerationService {

    private static final int DEMO_QUANTITY = 20;

    private final GeneratorFactory generatorFactory;
    private final FeatureAccessService featureAccessService;
    private final PDFService pdfService;

    /**
     * Генерация PDF для демо-сценария: фиксированное количество примеров сложения до 10.
     */
    public byte[] generateDemoForUser(Long userId, String title) {
        return generateArithmeticPdf(userId, OperationType.ADDITION_10, DEMO_QUANTITY, title);
    }

    /**
     * Общий метод генерации PDF для заданной операции.
     */
    public byte[] generateArithmeticPdf(Long userId,
                                        OperationType operationType,
                                        int quantity,
                                        String title) {
        AccessCheckResult access = featureAccessService.checkAccess(userId, Feature.PDF_GENERATION, 1);
        if (!access.isGranted()) {
            String message = access.getMessage() != null ? access.getMessage() : "Доступ к генерации PDF ограничен.";
            throw new PdfGenerationAccessException(message);
        }

        ArithmeticContext context = ArithmeticContext.builder()
                .operationType(operationType)
                .numberRange(defaultRange(operationType))
                .quantity(quantity)
                .noCarry(defaultNoCarry(operationType))
                .build()
                .validate();

        TaskGenerator generator = generatorFactory.getGenerator(operationType);
        List<Task> tasks = generator.generate(context);

        byte[] pdf = pdfService.generate(tasks, title);
        featureAccessService.incrementUsage(userId, Feature.PDF_GENERATION, 1);

        log.info("Generated PDF for user {}, operation {}, quantity {}", userId, operationType, quantity);
        return pdf;
    }

    private Range defaultRange(OperationType operationType) {
        return switch (operationType) {
            case ADDITION_10, SUBTRACTION_10 -> new Range(0, 10);
            case ADDITION_20_NO_CARRY, SUBTRACTION_20_NO_CARRY -> new Range(0, 20);
            case NUMBER_COMPOSITION_2_9 -> new Range(2, 9);
            case NUMBER_COMPOSITION_10 -> new Range(10, 10);
            case NUMBER_COMPOSITION_11_20 -> new Range(11, 20);
            case NUMBER_COMPOSITION -> new Range(2, 10);
            case COMPARISON, NUMBER_SEQUENCE -> new Range(0, 20);
        };
    }

    private Boolean defaultNoCarry(OperationType operationType) {
        return switch (operationType) {
            case ADDITION_20_NO_CARRY, SUBTRACTION_20_NO_CARRY -> Boolean.TRUE;
            default -> null;
        };
    }
}

