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
import java.awt.geom.NoninvertibleTransformException;
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
    private final Affine affine = new Affine(); // Поле класса для переиспользования

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

        gc.save();

        AffineTransform awtTransform = context.transform();
        affine.setToTransform(
            awtTransform.getScaleX(), awtTransform.getShearX(), awtTransform.getTranslateX(),
            awtTransform.getShearY(), awtTransform.getScaleY(), awtTransform.getTranslateY()
        );
        gc.setTransform(affine);

        if (context.gridVisible()) {
            renderGrid(context);
        }
        if (context.axisVisible()) {
            renderAxis(context);
        }

        for (Segment segment : context.segments()) {
            boolean isSelected = segment.equals(context.selectedSegment());
            renderSegment(segment, segment.style(), isSelected);
        }

        gc.restore();

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
        gc.setLineWidth(0.5 / context.transform().getScaleX());

        try {
            AffineTransform inverse = context.transform().createInverse();
            Point2D.Double topLeft = new Point2D.Double(0, 0);
            Point2D.Double bottomRight = new Point2D.Double(canvas.getWidth(), canvas.getHeight());

            inverse.transform(topLeft, topLeft);
            inverse.transform(bottomRight, bottomRight);

            double gridStep = context.gridStep();

            for (double x = Math.floor(topLeft.x / gridStep) * gridStep; x < bottomRight.x; x += gridStep) {
                gc.strokeLine(x, topLeft.y, x, bottomRight.y);
            }
            for (double y = Math.floor(topLeft.y / gridStep) * gridStep; y < bottomRight.y; y += gridStep) {
                gc.strokeLine(topLeft.x, y, bottomRight.x, y);
            }
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }
    }

    private void renderAxis(DrawingContext context) {
        gc.setLineDashes(0);
        gc.setStroke(Color.web(context.axisColor()));
        gc.setLineWidth(1.0 / context.transform().getScaleX());

        try {
            AffineTransform inverse = context.transform().createInverse();
            Point2D.Double topLeft = new Point2D.Double(0, 0);
            Point2D.Double bottomRight = new Point2D.Double(canvas.getWidth(), canvas.getHeight());

            inverse.transform(topLeft, topLeft);
            inverse.transform(bottomRight, bottomRight);

            gc.strokeLine(topLeft.x, 0, bottomRight.x, 0);
            gc.strokeLine(0, topLeft.y, 0, bottomRight.y);
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }
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
