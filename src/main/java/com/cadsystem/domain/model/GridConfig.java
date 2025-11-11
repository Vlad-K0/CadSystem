package com.cadsystem.domain.model;

/**
 * Конфигурация сетки.
 */
public record GridConfig(
        double gridStep,
        String gridColor,
        boolean showGrid,
        String axisColor,
        boolean showAxis
) {

    public static final GridConfig DEFAULT = new GridConfig(
            50.0, "#E0E0E0", true, "#000000", true
    );
}