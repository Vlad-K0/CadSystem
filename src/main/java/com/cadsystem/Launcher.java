package com.cadsystem;

/**
 * Класс-запускач.
 * Нужен для обхода проблем с модулями JavaFX при запуске из JAR
 * и для корректного старта на macOS.
 */
public class Launcher {
    public static void main(String[] args) {
        // Запускает основной класс приложения
        App.main(args);
    }
}