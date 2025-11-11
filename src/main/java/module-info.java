module com.cadsystem {
    requires javafx.controls;
    requires javafx.graphics;

    // Для логирования (если будешь использовать slf4j, как в твоих примерах)
    // requires org.slf4j;

    // Разрешает JavaFX доступ к твоим пакетам UI
    exports com.cadsystem;
    exports com.cadsystem.graphics.renderer;

    // Если будут проблемы с доступом к моделям
    exports com.cadsystem.domain.model;
}
