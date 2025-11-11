package com.cadsystem.domain.model;

/**
 * Перечисление для координатных систем.
 */
public enum CoordinateSystem {
    CARTESIAN("Декартова (x, y)"),
    POLAR("Полярная (r, θ)");

    private final String displayName;

    CoordinateSystem(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}