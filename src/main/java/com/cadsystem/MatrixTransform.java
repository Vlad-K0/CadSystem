package com.cadsystem;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Класс для управления аффинными преобразованиями видовой матрицы.
 * Инкапсулирует java.awt.geom.AffineTransform для реализации панорамирования,
 * масштабирования и поворота в соответствии с ТЗ.
 */
public class MatrixTransform {

    private AffineTransform transform = new AffineTransform();

    /**
     * Возвращает текущую матрицу преобразования.
     * @return Объект AffineTransform.
     */
    public AffineTransform getTransform() {
        return transform;
    }

    /**
     * Реализует панорамирование (сдвиг) вида.
     * Формула: x' = x + dx, y' = y + dy
     * @param dx Смещение по оси X.
     * @param dy Смещение по оси Y.
     */
    public void pan(double dx, double dy) {
        AffineTransform panTransform = AffineTransform.getTranslateInstance(dx, dy);
        transform.concatenate(panTransform);
    }

    /**
     * Реализует масштабирование относительно точки курсора мыши.
     * Логика: Сдвиг в начало координат -> Масштабирование -> Сдвиг обратно.
     * Формула: x' = x * s, y' = y * s
     * @param factor Коэффициент масштабирования (> 1 для увеличения, < 1 для уменьшения).
     * @param mouseX Координата X курсора.
     * @param mouseY Координата Y курсора.
     */
    public void zoom(double factor, double mouseX, double mouseY) {
        AffineTransform zoomTransform = new AffineTransform();
        zoomTransform.translate(mouseX, mouseY);
        zoomTransform.scale(factor, factor);
        zoomTransform.translate(-mouseX, -mouseY);
        transform.concatenate(zoomTransform);
    }

    /**
     * Реализует поворот вида относительно указанного центра.
     * Формула: x' = x*cos(θ) - y*sin(θ), y' = x*sin(θ) + y*cos(θ)
     * @param angle Угол поворота в радианах.
     * @param centerX Координата X центра вращения.
     * @param centerY Координата Y центра вращения.
     */
    public void rotate(double angle, double centerX, double centerY) {
        AffineTransform rotateTransform = AffineTransform.getRotateInstance(angle, centerX, centerY);
        transform.concatenate(rotateTransform);
    }

    /**
     * Сбрасывает все преобразования к исходному состоянию (единичная матрица).
     */
    public void reset() {
        transform.setToIdentity();
    }

    /**
     * Масштабирует и центрирует вид так, чтобы весь контент помещался в видимую область.
     * @param contentBounds Прямоугольник, описывающий границы контента.
     * @param viewBounds Прямоугольник, описывающий видимую область.
     */
    public void fitAll(Rectangle2D contentBounds, Rectangle2D viewBounds) {
        reset();

        double scaleX = viewBounds.getWidth() / contentBounds.getWidth();
        double scaleY = viewBounds.getHeight() / contentBounds.getHeight();
        double scale = Math.min(scaleX, scaleY);

        double transX = viewBounds.getX() + (viewBounds.getWidth() - contentBounds.getWidth() * scale) / 2.0;
        double transY = viewBounds.getY() + (viewBounds.getHeight() - contentBounds.getHeight() * scale) / 2.0;

        transform.translate(transX, transY);
        transform.scale(scale, scale);
        transform.translate(-contentBounds.getX(), -contentBounds.getY());
    }

    /**
     * Преобразует экранные координаты в мировые.
     * @param screenPoint Точка в экранных координатах.
     * @return Точка в мировых координатах или null, если преобразование невозможно.
     */
    public Point2D screenToWorld(Point2D screenPoint) {
        try {
            AffineTransform inverseTransform = transform.createInverse();
            Point2D worldPoint = new Point2D.Double();
            inverseTransform.transform(screenPoint, worldPoint);
            return worldPoint;
        } catch (NoninvertibleTransformException e) {
            System.err.println("Не удалось инвертировать матрицу: " + e.getMessage());
            return null;
        }
    }

    public double getScale() {
        return transform.getScaleX();
    }

    public double getRotationAngle() {
        return Math.atan2(transform.getShearY(), transform.getScaleX());
    }
}
