package com.cadsystem.graphics.render;

/**
 * Интерфейс рендерера.
 */
public interface Renderer {
    void render(DrawingContext context);

    // Добавляем метод для обновления Canvas, если он нам нужен
    void setCanvas(javafx.scene.canvas.Canvas canvas);
}