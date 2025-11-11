package com.cadsystem.domain.model;

import java.io.Serializable;

/**
 * Точка в декартовой координатной системе.
 * Неизменяемый data carrier (Record).
 */
public record Point(double x, double y) implements Serializable {

    public Point {
        if (Double.isNaN(x) || Double.isNaN(y)) {
            throw new IllegalArgumentException(
                    "Координаты не могут быть NaN: x=" + x + ", y=" + y
            );
        }
        if (Double.isInfinite(x) || Double.isInfinite(y)) {
            throw new IllegalArgumentException(
                    "Координаты не могут быть бесконечностью"
            );
        }
    }

    /**
     * Расстояние от начала координат.
     */
    public double distanceFromOrigin() {
        return Math.sqrt(x * x + y * y);
    }

    /**
     * Расстояние до другой точки.
     */
    public double distanceTo(Point other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}