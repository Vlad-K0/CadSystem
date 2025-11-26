module com.cadsystem {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml; // Добавлено, так как есть в pom.xml
    requires javafx.base;  // Добавлено для работы Properties

    // Для логирования
    requires org.slf4j;
    requires java.desktop;

    // Разрешает JavaFX доступ к вашим пакетам UI
    exports com.cadsystem;
    exports com.cadsystem.graphics.render;
    exports com.cadsystem.domain.model;
    exports com.cadsystem.domain.event;

    // Открываем ViewModel для рефлексии JavaFX (для биндинга Properties)
    opens com.cadsystem.ui.viewmodel to javafx.base;
}
