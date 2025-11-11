package com.cadsystem.domain.model;

import java.io.Serializable;

/**
 * Стиль отрезка (цвет, толщина и т.д.).
 */
public record LineStyle(
        String color,
        double thickness,
        String strokeType  // SOLID, DASHED, DOTTED
) implements Serializable {

    public static final LineStyle DEFAULT = new LineStyle("#0066FF", 2.0, "SOLID");

    public LineStyle {
        if (thickness <= 0) {
            throw new IllegalArgumentException("Толщина должна быть > 0");
        }
    }
}