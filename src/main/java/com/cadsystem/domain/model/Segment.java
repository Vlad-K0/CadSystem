package com.cadsystem.domain.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Отрезок, определяемый двумя точками.
 * Неизменяемый data carrier.
 */
public record Segment(Point start, Point end) implements Serializable {

    public Segment {
        Objects.requireNonNull(start, "Начальная точка не может быть null");
        Objects.requireNonNull(end, "Конечная точка не может быть null");

        if (start.equals(end)) {
            throw new IllegalArgumentException(
                    "Начальная и конечная точки не могут совпадать"
            );
        }
    }

    /**
     * Длина отрезка.
     */
    public double length() {
        return start.distanceTo(end);
    }

    /**
     * Угол наклона отрезка в радианах.
     */
    public double angleRadians() {
        return Math.atan2(end.y() - start.y(), end.x() - start.x());
    }

    /**
     * Угол наклона отрезка в градусах.
     */
    public double angleDegrees() {
        return Math.toDegrees(angleRadians());
    }

    /**
     * Нормализованное представление (для визуализации).
     */
    @Override
    public String toString() {
        return String.format(
                "Segment[start=(%.2f,%.2f), end=(%.2f,%.2f), length=%.2f]",
                start.x(), start.y(), end.x(), end.y(), length()
        );
    }
}