package com.cadsystem.domain.service;

import com.cadsystem.domain.model.Point;
import com.cadsystem.domain.model.Segment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Калькулятор геометрических свойств.
 */
public class GeometryCalculator {

    private static final Logger logger = LoggerFactory.getLogger(
            GeometryCalculator.class
    );

    /**
     * Вычисляет параметры отрезка.
     */
    public SegmentProperties calculateSegmentProperties(Segment segment) {
        double length = segment.length();
        double angle = segment.angleDegrees();

        logger.debug("Параметры отрезка: длина={}, угол={}", length, angle);

        return new SegmentProperties(
                segment.start(),
                segment.end(),
                length,
                angle
        );
    }

    /**
     * Проверяет, пересекаются ли два отрезка.
     */
    public boolean segmentsIntersect(Segment s1, Segment s2) {
        // Используем ориентированное произведение для определения положения
        return doIntersect(
                s1.start(), s1.end(), s2.start(),
                s2.end()
        );
    }

    private boolean doIntersect(Point p1, Point p2, Point p3, Point p4) {
        double o1 = orientation(p1, p2, p3);
        double o2 = orientation(p1, p2, p4);
        double o3 = orientation(p3, p4, p1);
        double o4 = orientation(p3, p4, p2);

        return (o1 * o2 < 0) && (o3 * o4 < 0);
    }

    private double orientation(Point p, Point q, Point r) {
        // (y2 - y1) * (x3 - x2) - (x2 - x1) * (y3 - y2)
        double val = (q.y() - p.y()) * (r.x() - q.x()) -
                     (q.x() - p.x()) * (r.y() - q.y());
        return val;
    }

    public double distanceToPoint(Segment segment, Point point) {
        double l2 = segment.length() * segment.length();
        if (l2 == 0.0) return point.distanceTo(segment.start());

        double t = ((point.x() - segment.start().x()) * (segment.end().x() - segment.start().x()) +
                    (point.y() - segment.start().y()) * (segment.end().y() - segment.start().y())) / l2;
        t = Math.max(0, Math.min(1, t));

        Point projection = new Point(
                segment.start().x() + t * (segment.end().x() - segment.start().x()),
                segment.start().y() + t * (segment.end().y() - segment.start().y())
        );

        return point.distanceTo(projection);
    }
}