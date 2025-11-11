package com.cadsystem.domain.service;

import com.cadsystem.domain.model.AngleUnit;
import com.cadsystem.domain.model.Point;

/**
 * Интерфейс для преобразования координат между системами.
 */
public interface CoordinateTransformer {
    /**
     * Преобразует точку из полярных координат в декартовы.
     * Формула: x = r * cos(θ), y = r * sin(θ)
     */
    Point polarToCartesian(double r, double theta, AngleUnit angleUnit);

    /**
     * Преобразует точку из декартовых координат в полярные.
     * Формула: r = sqrt(x² + y²), θ = atan2(y, x)
     */
    Point cartesianToPolar(double x, double y, AngleUnit angleUnit);
}