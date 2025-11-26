package com.cadsystem;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class ViewCanvas extends Canvas {

    private MatrixTransform matrixTransform = new MatrixTransform();
    private Rectangle2D.Double content = new Rectangle2D.Double(50, 50, 200, 150);
    private double lastMouseX, lastMouseY;
    private boolean handToolActive = false;
    private Label statusBar;

    public ViewCanvas(Label statusBar) {
        this.statusBar = statusBar;

        setOnMousePressed(this::handleMousePressed);
        setOnMouseDragged(this::handleMouseDragged);
        setOnMouseMoved(this::handleMouseMoved);

        setOnScroll(event -> {
            double scaleFactor = event.getDeltaY() > 0 ? 1.1 : 1 / 1.1;
            matrixTransform.zoom(scaleFactor, event.getX(), event.getY());
            draw();
            updateStatus(event.getX(), event.getY());
        });

        this.setFocusTraversable(true);
        this.setOnKeyPressed(this::handleKeyPressed);

        draw();
        updateStatus(0, 0);
    }

    private void handleMousePressed(MouseEvent event) {
        if (event.getButton() == MouseButton.MIDDLE || (handToolActive && event.getButton() == MouseButton.PRIMARY)) {
            lastMouseX = event.getX();
            lastMouseY = event.getY();
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        if (event.isMiddleButtonDown() || (handToolActive && event.isPrimaryButtonDown())) {
            double dx = event.getX() - lastMouseX;
            double dy = event.getY() - lastMouseY;
            matrixTransform.pan(dx, dy);
            lastMouseX = event.getX();
            lastMouseY = event.getY();
            draw();
            updateStatus(event.getX(), event.getY());
        }
    }

    private void handleMouseMoved(MouseEvent event) {
        updateStatus(event.getX(), event.getY());
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.PLUS || event.getCode() == KeyCode.EQUALS) {
            zoomIn();
        } else if (event.getCode() == KeyCode.MINUS) {
            zoomOut();
        } else if (event.getCode() == KeyCode.LEFT) {
            rotateLeft(event.isShiftDown());
        } else if (event.getCode() == KeyCode.RIGHT) {
            rotateRight(event.isShiftDown());
        }
    }

    public void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());
        gc.save();
        java.awt.geom.AffineTransform awtTransform = matrixTransform.getTransform();
        gc.setTransform(new Affine(
            awtTransform.getScaleX(), awtTransform.getShearX(), awtTransform.getTranslateX(),
            awtTransform.getShearY(), awtTransform.getScaleY(), awtTransform.getTranslateY()
        ));
        gc.setFill(Color.BLUE);
        gc.fillRect(content.x, content.y, content.width, content.height);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(content.x, content.y, content.width, content.height);
        gc.restore();
    }

    public void updateStatus(double mouseX, double mouseY) {
        double scale = matrixTransform.getScale();
        double angle = Math.toDegrees(matrixTransform.getRotationAngle());
        Point2D worldCoords = matrixTransform.screenToWorld(new Point2D.Double(mouseX, mouseY));

        String status = String.format(
            "Scale: %.2f%% | Angle: %.1f° | Coords: (X: %.2f, Y: %.2f)",
            scale * 100, angle, worldCoords.getX(), worldCoords.getY()
        );
        statusBar.setText(status);
    }

    public void zoomIn() {
        matrixTransform.zoom(1.1, getWidth() / 2.0, getHeight() / 2.0);
        draw();
        updateStatus(getWidth() / 2.0, getHeight() / 2.0);
    }

    public void zoomOut() {
        matrixTransform.zoom(1 / 1.1, getWidth() / 2.0, getHeight() / 2.0);
        draw();
        updateStatus(getWidth() / 2.0, getHeight() / 2.0);
    }

    public void rotateLeft(boolean isShiftDown) {
        double angle = isShiftDown ? 90.0 : 15.0;
        matrixTransform.rotate(-Math.toRadians(angle), getWidth() / 2.0, getHeight() / 2.0);
        draw();
        updateStatus(getWidth() / 2.0, getHeight() / 2.0);
    }

    public void rotateRight(boolean isShiftDown) {
        double angle = isShiftDown ? 90.0 : 15.0;
        matrixTransform.rotate(Math.toRadians(angle), getWidth() / 2.0, getHeight() / 2.0);
        draw();
        updateStatus(getWidth() / 2.0, getHeight() / 2.0);
    }

    public void fitAll() {
        java.awt.geom.Rectangle2D viewBounds = new java.awt.geom.Rectangle2D.Double(0, 0, getWidth(), getHeight());
        matrixTransform.fitAll(content.getBounds2D(), viewBounds);
        draw();
        updateStatus(getWidth() / 2.0, getHeight() / 2.0);
    }

    public void resetView() {
        matrixTransform.reset();
        draw();
        updateStatus(getWidth() / 2.0, getHeight() / 2.0);
    }

    public void setHandToolActive(boolean active) {
        this.handToolActive = active;
    }

    public MatrixTransform getMatrixTransform() {
        return matrixTransform;
    }
}
