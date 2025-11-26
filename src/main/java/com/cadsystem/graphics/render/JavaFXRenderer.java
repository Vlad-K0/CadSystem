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
import java.awt.geom.Point2D;
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

        // Рисуем сетку и оси БЕЗ трансформации вида
        if (context.gridVisible()) {
            renderGrid(context);
        }
        if (context.axisVisible()) {
            renderAxis(context);
        }

        // Сохраняем состояние gc без трансформаций
        gc.save();

        // Применяем матрицу вида
        AffineTransform awtTransform = context.transform();
        gc.setTransform(new Affine(
            awtTransform.getScaleX(), awtTransform.getShearX(), awtTransform.getTranslateX(),
            awtTransform.getShearY(), awtTransform.getScaleY(), awtTransform.getTranslateY()
        ));

        for (Segment segment : context.segments()) {
            boolean isSelected = segment.equals(context.selectedSegment());
            renderSegment(segment, segment.style(), isSelected);
        }

        // Восстанавливаем gc к состоянию без трансформаций
        gc.restore();

        // Рисуем маркеры поверх всего, уже в экранных координатах
        for (Segment segment : context.segments()) {
            renderMarkers(segment, segment.style(), context.transform());
        }

        logger.debug("Отрисовка завершена");
    }

    private void clearCanvas(String backgroundColor) {
        Paint paint = Color.web(backgroundColor);
        gc.setFill(paint);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void renderGrid(DrawingContext context) {
        gc.setLineDashes(0);
        gc.setStroke(Color.web(context.gridColor()));
        gc.setLineWidth(0.5);

        AffineTransform transform = context.transform();
        double scale = transform.getScaleX();
        double gridStep = context.gridStep() * scale;
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        double offsetX = transform.getTranslateX();
        double offsetY = transform.getTranslateY();

        // Вертикальные линии
        for (double x = offsetX % gridStep; x < width; x += gridStep) {
            gc.strokeLine(x, 0, x, height);
        }

        // Горизонтальные линии
        for (double y = offsetY % gridStep; y < height; y += gridStep) {
            gc.strokeLine(0, y, width, y);
        }
    }

    private void renderAxis(DrawingContext context) {
        gc.setLineDashes(0);
        gc.setStroke(Color.web(context.axisColor()));
        gc.setLineWidth(1.0);

        AffineTransform transform = context.transform();
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double centerX = transform.getTranslateX();
        double centerY = transform.getTranslateY();

        // Ось X
        gc.strokeLine(0, centerY, width, centerY);
        // Ось Y
        gc.strokeLine(centerX, 0, centerX, height);
    }

    private void renderSegment(Segment segment, LineStyle style, boolean isSelected) {
        gc.setStroke(Color.web(style.color()));
        gc.setLineWidth(style.thickness());

        if (isSelected) {
            gc.setLineDashes(10, 5);
        } else {
            gc.setLineDashes(0);
        }

        double x1 = segment.start().x();
        double y1 = segment.start().y();
        double x2 = segment.end().x();
        double y2 = segment.end().y();

        gc.strokeLine(x1, y1, x2, y2);
    }

    private void renderMarkers(Segment segment, LineStyle style, AffineTransform transform) {
        Point2D.Double p1 = new Point2D.Double(segment.start().x(), segment.start().y());
        Point2D.Double p2 = new Point2D.Double(segment.end().x(), segment.end().y());

        transform.transform(p1, p1);
        transform.transform(p2, p2);

        double markerSize = 6;
        gc.setFill(Color.web(style.color()));
        gc.fillOval(p1.x - markerSize / 2, p1.y - markerSize / 2, markerSize, markerSize);
        gc.fillOval(p2.x - markerSize / 2, p2.y - markerSize / 2, markerSize, markerSize);
    }
}
