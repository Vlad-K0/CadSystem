package com.cadsystem.domain.service;

import com.cadsystem.domain.model.Point;

/**
 * Свойства отрезка.
 */
public record SegmentProperties(
        Point startPoint,
        Point endPoint,
        double length,
        double angleDegrees
) { }