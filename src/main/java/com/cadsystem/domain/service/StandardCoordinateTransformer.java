package com.cadsystem.domain.service;

import com.cadsystem.domain.model.AngleUnit;
import com.cadsystem.domain.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Реализация преобразователя координат.
 */
public class StandardCoordinateTransformer implements CoordinateTransformer {

    private static final double EPSILON = 1e-10;
    private static final Logger logger = LoggerFactory.getLogger(
            StandardCoordinateTransformer.class
    );

    @Override
    public Point polarToCartesian(double r, double theta, AngleUnit angleUnit) {
        if (r < 0) {
            throw new IllegalArgumentException(
                    "Радиус не может быть отрицательным: r=" + r
            );
        }

        // Нормализуем угол
        double thetaRadians = angleUnit.toRadians(theta);
        double x = r * Math.cos(thetaRadians);
        double y = r * Math.sin(thetaRadians);

        // Избегаем -0.0
        if (Math.abs(x) < EPSILON) x = 0.0;
        if (Math.abs(y) < EPSILON) y = 0.0;

        logger.debug("Полярные→Декартовы: (r={}, θ={}) → (x={}, y={})",
                r, theta, x, y);

        return new Point(x, y);
    }

    @Override
    public Point cartesianToPolar(double x, double y, AngleUnit angleUnit) {
        double r = Math.sqrt(x * x + y * y);
        double theta = Math.atan2(y, x);

        // Нормализуем угол в диапазон [0, 360) для градусов
        if (angleUnit == AngleUnit.DEGREES) {
            theta = Math.toDegrees(theta);
            if (theta < 0) {
                theta += 360.0;
            }
        }

        logger.debug("Декартовы→Полярные: (x={}, y={}) → (r={}, θ={})",
                x, y, r, theta);

        return new Point(r, theta);
    }
}