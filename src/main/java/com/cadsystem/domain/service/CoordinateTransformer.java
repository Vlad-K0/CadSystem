package com.cadsystem.domain.service;

import com.cadsystem.domain.model.AngleUnit;
import com.cadsystem.domain.model.Point;

/**
 * Интерфейс для преобразования координат между системами.
 */
public interface CoordinateTransformer {
    Point toCartesian(Point polarPoint, AngleUnit angleUnit);

    Point toPolar(Point cartesianPoint, AngleUnit angleUnit);
}