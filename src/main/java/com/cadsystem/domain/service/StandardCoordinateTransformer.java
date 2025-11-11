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
    public Point toCartesian(Point polarPoint, AngleUnit angleUnit) {
        double r = polarPoint.x();
        double theta = polarPoint.y();

        if (r < 0) {
            throw new IllegalArgumentException(
                    "Радиус не может быть отрицательным: r=" + r
            );
        }

        double thetaRadians = angleUnit.toRadians(theta);
        double x = r * Math.cos(thetaRadians);
        double y = r * Math.sin(thetaRadians);

        if (Math.abs(x) < EPSILON) x = 0.0;
        if (Math.abs(y) < EPSILON) y = 0.0;

        logger.debug("Полярные→Декартовы: {} → (x={}, y={})",
                polarPoint, x, y);

        return new Point(x, y);
    }

    @Override
    public Point toPolar(Point cartesianPoint, AngleUnit angleUnit) {
        double x = cartesianPoint.x();
        double y = cartesianPoint.y();

        double r = Math.sqrt(x * x + y * y);
        double theta = Math.atan2(y, x);

        if (angleUnit == AngleUnit.DEGREES) {
            theta = Math.toDegrees(theta);
            if (theta < 0) {
                theta += 360.0;
            }
        }

        logger.debug("Декартовы→Полярные: {} → (r={}, θ={})",
                cartesianPoint, r, theta);

        return new Point(r, theta);
    }
}