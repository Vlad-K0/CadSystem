package com.cadsystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.awt.geom.Point2D;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Минимальные тесты для проверки корректности математических операций в MatrixTransform.
 */
public class MinimalTests {

    private MatrixTransform matrixTransform;

    @BeforeEach
    public void setUp() {
        matrixTransform = new MatrixTransform();
    }

    @Test
    public void testPan() {
        Point2D.Double p = new Point2D.Double(10, 20);
        matrixTransform.pan(5, -10);
        matrixTransform.getTransform().transform(p, p);
        assertEquals(15.0, p.x, "Pan X coordinate should be 15.0");
        assertEquals(10.0, p.y, "Pan Y coordinate should be 10.0");
    }

    @Test
    public void testZoom() {
        Point2D.Double p = new Point2D.Double(10, 10);
        // Зум в 2 раза относительно точки (0,0)
        matrixTransform.zoom(2.0, 0, 0);
        matrixTransform.getTransform().transform(p, p);
        assertEquals(20.0, p.x, "Zoom X coordinate should be 20.0");
        assertEquals(20.0, p.y, "Zoom Y coordinate should be 20.0");
    }

    @Test
    public void testZoomAroundPoint() {
        Point2D.Double p = new Point2D.Double(20, 20);
        // Зум в 2 раза относительно точки (10,10)
        matrixTransform.zoom(2.0, 10, 10);
        matrixTransform.getTransform().transform(p, p);
        // Ожидаемый результат: (10, 10) + (10, 10) * 2 = (30, 30)
        assertEquals(30.0, p.x, "Zoom around point X coordinate should be 30.0");
        assertEquals(30.0, p.y, "Zoom around point Y coordinate should be 30.0");
    }

    @Test
    public void testRotate() {
        Point2D.Double p = new Point2D.Double(10, 0);
        // Поворот на 90 градусов (PI/2) вокруг (0,0)
        matrixTransform.rotate(Math.PI / 2, 0, 0);
        matrixTransform.getTransform().transform(p, p);
        assertEquals(0.0, p.x, 1e-9, "Rotate X coordinate should be close to 0.0");
        assertEquals(10.0, p.y, 1e-9, "Rotate Y coordinate should be close to 10.0");
    }

    @Test
    public void testScreenToWorld() {
        matrixTransform.pan(10, 20);
        matrixTransform.zoom(2.0, 0, 0);
        Point2D.Double screenPoint = new Point2D.Double(30, 40);
        Point2D worldPoint = matrixTransform.screenToWorld(screenPoint);
        // Обратное преобразование: (30-10)/2=10, (40-20)/2=10
        assertNotNull(worldPoint, "World point should not be null");
        assertEquals(10.0, worldPoint.getX(), "screenToWorld X coordinate should be 10.0");
        assertEquals(10.0, worldPoint.getY(), "screenToWorld Y coordinate should be 10.0");
    }

    @Test
    public void testReset() {
        matrixTransform.pan(10, 10);
        matrixTransform.zoom(2.0, 0, 0);
        matrixTransform.reset();
        assertTrue(matrixTransform.getTransform().isIdentity(), "Transform should be identity after reset");
    }
}
