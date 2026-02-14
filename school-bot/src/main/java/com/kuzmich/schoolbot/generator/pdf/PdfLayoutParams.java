package com.kuzmich.schoolbot.generator.pdf;

import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * Параметры раскладки одной страницы PDF с заданиями: размер шрифта, интервалы, сетка колонок/строк.
 * Вычисляются по количеству заданий и формату страницы (A4), чтобы всё помещалось с комфортным
 * кеглем и «воздухом» между примерами (подход «сетка»).
 */
public record PdfLayoutParams(
        int columns,
        int rows,
        float taskFontSize,
        float lineHeight,
        float titleFontSize,
        float contentStartY,
        float columnWidth,
        float leftColumnX,
        float rightColumnX,
        float marginLeft,
        float marginTop,
        float columnGap,
        float pageWidth,
        float pageHeight
) {

    private static final float MARGIN_LEFT = 50f;
    private static final float MARGIN_TOP = 50f;
    private static final float COLUMN_GAP = 40f;
    /** Высота блока заголовка (заголовок + дата) в пунктах. */
    private static final float HEADER_HEIGHT = 55f;
    /** Доля высоты ячейки, отводимая на кегль (остальное — воздух между примерами). */
    private static final float FONT_TO_CELL_RATIO = 0.6f;
    /** Межстрочный интервал как множитель от кегля. */
    private static final float LINE_HEIGHT_RATIO = 1.25f;
    private static final float MIN_TASK_FONT_SIZE = 14f;
    private static final float MAX_TASK_FONT_SIZE = 28f;
    private static final float TITLE_FONT_SIZE = 16f;

    /**
     * Строит параметры раскладки по количеству заданий для страницы A4.
     * Колонки: 1 при ≤10 заданиях, иначе 2. Высота ячейки и кегль подбираются так,
     * чтобы всё помещалось с комфортным чтением для младших школьников.
     */
    public static PdfLayoutParams forTaskCount(int taskCount, float pageWidth, float pageHeight) {
        if (taskCount <= 0) {
            taskCount = 1;
        }

        int columns = taskCount <= 10 ? 1 : 2;
        int rows = (int) Math.ceil((double) taskCount / columns);

        float usableHeight = pageHeight - MARGIN_TOP - MARGIN_LEFT - HEADER_HEIGHT;
        float cellHeight = usableHeight / rows;

        float taskFontSize = cellHeight * FONT_TO_CELL_RATIO;
        if (taskFontSize < MIN_TASK_FONT_SIZE) {
            taskFontSize = MIN_TASK_FONT_SIZE;
        } else if (taskFontSize > MAX_TASK_FONT_SIZE) {
            taskFontSize = MAX_TASK_FONT_SIZE;
        }

        float lineHeight = taskFontSize * LINE_HEIGHT_RATIO;

        float contentWidth = pageWidth - 2 * MARGIN_LEFT;
        float columnWidth = (contentWidth - (columns > 1 ? COLUMN_GAP : 0)) / columns;
        float leftColumnX = MARGIN_LEFT;
        float rightColumnX = MARGIN_LEFT + columnWidth + COLUMN_GAP;

        float contentStartY = pageHeight - MARGIN_TOP - HEADER_HEIGHT;

        return new PdfLayoutParams(
                columns,
                rows,
                taskFontSize,
                lineHeight,
                TITLE_FONT_SIZE,
                contentStartY,
                columnWidth,
                leftColumnX,
                rightColumnX,
                MARGIN_LEFT,
                MARGIN_TOP,
                COLUMN_GAP,
                pageWidth,
                pageHeight
        );
    }

    /**
     * Удобный вызов для A4 (ширина и высота берутся из PDRectangle.A4).
     */
    public static PdfLayoutParams forTaskCountA4(int taskCount) {
        PDRectangle a4 = PDRectangle.A4;
        return forTaskCount(taskCount, a4.getWidth(), a4.getHeight());
    }

    /**
     * X-координата колонки по индексу (0 — левая, 1 — правая).
     */
    public float columnX(int columnIndex) {
        return columnIndex == 0 ? leftColumnX : rightColumnX;
    }

    /**
     * Y-координата для строки с заданным индексом (0 — первая строка под заголовком).
     */
    public float rowY(int rowIndex) {
        return contentStartY - rowIndex * lineHeight;
    }
}
