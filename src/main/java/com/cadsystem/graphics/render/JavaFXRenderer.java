package com.cadsystem.graphics.render;

import com.cadsystem.app.EventBus;
import com.cadsystem.domain.model.LineStyle;
import com.cadsystem.domain.model.Segment;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Рендерер на основе JavaFX Canvas.
 */
public class JavaFXRenderer implements Renderer {

    private static final Logger logger = LoggerFactory.getLogger(
            JavaFXRenderer.class
    );

    private Canvas canvas;
    private GraphicsContext gc;
    private final EventBus eventBus;

    public JavaFXRenderer(Canvas canvas, EventBus eventBus) {
        this.eventBus = Objects.requireNonNull(eventBus);
        setCanvas(canvas); // Используем метод для инициализации
        logger.debug("JavaFX Renderer инициализирован");
    }

    @Override
    public void setCanvas(Canvas canvas) {
        this.canvas = Objects.requireNonNull(canvas);
        this.gc = canvas.getGraphicsContext2D();

        // Оптимизация для Retina/HiDPI
        // Мы будем рисовать четко, отключая сглаживание пикселей
        gc.setImageSmoothing(false);
    }

    @Override
    public void render(DrawingContext context) {
        if (canvas == null) {
            logger.warn("Canvas не установлен, отрисовка пропущена.");
            return;
        }
        Objects.requireNonNull(context);

        // Очищаем canvas
        clearCanvas(context.backgroundColor());

        // Рисуем компоненты в правильном порядке
        if (context.gridVisible()) {
            renderGrid(context);
        }

        if (context.axisVisible()) {
            renderAxis(context);
        }

        // Рисуем геометрические объекты
        for (Segment segment : context.segments()) {
            boolean isSelected = segment.equals(context.selectedSegment());
            renderSegment(segment, context.segmentStyle(), isSelected);
        }

        logger.debug("Отрисовка завершена");
    }

    private void clearCanvas(String backgroundColor) {
        Paint paint = Color.web(backgroundColor);
        gc.setFill(paint);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void renderGrid(DrawingContext context) {
        gc.setStroke(Color.web(context.gridColor()));
        gc.setLineWidth(0.5);

        double gridStep = context.gridStep();
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        // Центр (0,0) в декартовых координатах
        double centerX = width / 2;
        double centerY = height / 2;

        // Вертикальные линии
        for (double x = centerX + gridStep; x < width; x += gridStep) {
            gc.strokeLine(x, 0, x, height);
        }
        for (double x = centerX - gridStep; x > 0; x -= gridStep) {
            gc.strokeLine(x, 0, x, height);
        }

        // Горизонтальные линии
        for (double y = centerY + gridStep; y < height; y += gridStep) {
            gc.strokeLine(0, y, width, y);
        }
        for (double y = centerY - gridStep; y > 0; y -= gridStep) {
            gc.strokeLine(0, y, width, y);
        }

        logger.debug("Сетка отрисована с шагом {}", gridStep);
    }

    private void renderAxis(DrawingContext context) {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double centerX = width / 2;
        double centerY = height / 2;

        gc.setStroke(Color.web(context.axisColor()));
        gc.setLineWidth(1.0); // Оси могут быть чуть толще сетки

        // Ось X
        gc.strokeLine(0, centerY, width, centerY);

        // Ось Y
        gc.strokeLine(centerX, 0, centerX, height);

        logger.debug("Оси координат отрисованы");
    }

    private void renderSegment(Segment segment, LineStyle style, boolean isSelected) {
        gc.setStroke(Color.web(style.color()));
        gc.setLineWidth(style.thickness());

        if (isSelected) {
            gc.setLineDashes(10, 5);
        } else {
            gc.setLineDashes(0);
        }

        // Преобразуем координаты в экранные координаты
        double x1 = canvasXFromCoordinate(segment.start().x());
        double y1 = canvasYFromCoordinate(segment.start().y());
        double x2 = canvasXFromCoordinate(segment.end().x());
        double y2 = canvasYFromCoordinate(segment.end().y());

        gc.strokeLine(x1, y1, x2, y2);

        // Рисуем маркеры точек
        double markerSize = 6;
        gc.setFill(Color.web(style.color()));
        gc.fillOval(x1 - markerSize / 2, y1 - markerSize / 2,
                markerSize, markerSize);
        gc.fillOval(x2 - markerSize / 2, y2 - markerSize / 2,
                markerSize, markerSize);

        logger.debug("Отрезок отрисован: {}, isSelected: {}", segment, isSelected);
    }

    // Преобразование декартовой X в X канваса
    private double canvasXFromCoordinate(double x) {
        return canvas.getWidth() / 2 + x;
    }

    // Преобразование декартовой Y в Y канваса (Y инвертирована)
    private double canvasYFromCoordinate(double y) {
        return canvas.getHeight() / 2 - y;
    }
}