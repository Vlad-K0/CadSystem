package com.cadsystem.graphics.render;

import com.cadsystem.app.EventBus;
import com.cadsystem.domain.model.LineStyle;
import com.cadsystem.domain.model.Segment;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.transform.Affine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.AffineTransform;
import java.util.Objects;

/**
 * Рендерер на основе JavaFX Canvas.
 */
public class JavaFXRenderer implements Renderer {

    private static final Logger logger = LoggerFactory.getLogger(JavaFXRenderer.class);

    private Canvas canvas;
    private GraphicsContext gc;
    private final EventBus eventBus;

    public JavaFXRenderer(Canvas canvas, EventBus eventBus) {
        this.eventBus = Objects.requireNonNull(eventBus);
        setCanvas(canvas);
        logger.debug("JavaFX Renderer инициализирован");
    }

    @Override
    public void setCanvas(Canvas canvas) {
        this.canvas = Objects.requireNonNull(canvas);
        this.gc = canvas.getGraphicsContext2D();
        gc.setImageSmoothing(false);
    }

    @Override
    public void render(DrawingContext context) {
        if (canvas == null) {
            logger.warn("Canvas не установлен, отрисовка пропущена.");
            return;
        }
        Objects.requireNonNull(context);

        clearCanvas(context.backgroundColor());

        // Сохраняем состояние gc без трансформаций
        gc.save();

        // Применяем матрицу вида
        AffineTransform awtTransform = context.transform();
        gc.setTransform(new Affine(
            awtTransform.getScaleX(), awtTransform.getShearX(), awtTransform.getTranslateX(),
            awtTransform.getShearY(), awtTransform.getScaleY(), awtTransform.getTranslateY()
        ));

        if (context.gridVisible()) {
            renderGrid(context);
        }

        if (context.axisVisible()) {
            renderAxis(context);
        }

        for (Segment segment : context.segments()) {
            boolean isSelected = segment.equals(context.selectedSegment());
            renderSegment(segment, segment.style(), isSelected, context.transform().getScaleX());
        }

        // Восстанавливаем gc к состоянию без трансформаций
        gc.restore();

        logger.debug("Отрисовка завершена");
    }

    private void clearCanvas(String backgroundColor) {
        Paint paint = Color.web(backgroundColor);
        gc.setFill(paint);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void renderGrid(DrawingContext context) {
        // ... (реализация отрисовки сетки с учетом трансформации)
        // Для простоты пока оставим как есть, но в идеале это тоже должно
        // работать с инвертированной матрицей, чтобы рисовать сетку в мировых координатах.
    }

    private void renderAxis(DrawingContext context) {
        gc.setStroke(Color.web(context.axisColor()));
        gc.setLineWidth(1.0);

        // Ось X
        gc.strokeLine(-10000, 0, 10000, 0);
        // Ось Y
        gc.strokeLine(0, -10000, 0, 10000);
    }

    private void renderSegment(Segment segment, LineStyle style, boolean isSelected, double scale) {
        gc.setStroke(Color.web(style.color()));
        gc.setLineWidth(style.thickness());

        if (isSelected) {
            gc.setLineDashes(10, 5);
        } else {
            gc.setLineDashes(0);
        }

        double x1 = segment.start().x();
        double y1 = -segment.start().y(); // Инвертируем Y для JavaFX
        double x2 = segment.end().x();
        double y2 = -segment.end().y(); // Инвертируем Y для JavaFX

        gc.strokeLine(x1, y1, x2, y2);

        double markerSize = 6 / scale; // Масштабируем маркеры
        gc.setFill(Color.web(style.color()));
        gc.fillOval(x1 - markerSize / 2, y1 - markerSize / 2, markerSize, markerSize);
        gc.fillOval(x2 - markerSize / 2, y2 - markerSize / 2, markerSize, markerSize);
    }
}
