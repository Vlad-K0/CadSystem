package com.cadsystem.graphics.render;

import com.cadsystem.domain.model.LineStyle;
import com.cadsystem.domain.model.Segment;
import java.awt.geom.AffineTransform;

import java.util.List;

/**
 * Контекст для рендеринга.
 * Содержит все, что нужно знать рендереру для отрисовки кадра.
 */
public record DrawingContext(
        List<Segment> segments,
        Segment selectedSegment,
        String backgroundColor,
        String gridColor,
        String axisColor,
        double gridStep,
        boolean gridVisible,
        boolean axisVisible,
        AffineTransform transform
) { }
