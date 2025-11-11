package com.cadsystem.domain.model;

/**
 * Единицы измерения углов.
 */
public enum AngleUnit {
    DEGREES("Градусы", "°", 360.0),
    RADIANS("Радианы", "рад", 2 * Math.PI);

    private final String displayName;
    private final String symbol;
    private final double fullRotation;

    AngleUnit(String displayName, String symbol, double fullRotation) {
        this.displayName = displayName;
        this.symbol = symbol;
        this.fullRotation = fullRotation;
    }

    public double toRadians(double angle) {
        return this == RADIANS ? angle : Math.toRadians(angle);
    }

    public double toDegrees(double angle) {
        return this == DEGREES ? angle : Math.toDegrees(angle);
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }
}