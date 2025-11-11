package com.cadsystem;

import com.cadsystem.app.ApplicationConfiguration;
import com.cadsystem.app.DependencyInjectionContainer;
import com.cadsystem.app.EventBus;
import com.cadsystem.domain.model.LineStyle;
import com.cadsystem.domain.model.Point;
import com.cadsystem.domain.model.Segment;
import com.cadsystem.graphics.render.DrawingContext;
import com.cadsystem.graphics.render.JavaFXRenderer;
import com.cadsystem.graphics.render.Renderer;
import com.cadsystem.ui.viewmodel.MainViewModel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * JavaFX App
 */
public class App extends Application {

    private DependencyInjectionContainer container;
    private MainViewModel viewModel;
    private Renderer renderer;
    private Canvas canvas;

    @Override
    public void start(Stage stage) {
        // 1. Инициализация DI контейнера и ViewModel
        container = ApplicationConfiguration.createDIContainer();
        ApplicationConfiguration.configureEventSubscriptions(container);

        // Получаем ViewModel из DI
        viewModel = container.resolve(MainViewModel.class);

        // 2. Настройка Canvas (холста)
        canvas = new Canvas(800, 600);

        // 3. Создаем Renderer и передаем ему Canvas
        renderer = new JavaFXRenderer(canvas, container.resolve(EventBus.class));

        // 4. Создаем тестовый UI (позже замени
        VBox controls = new VBox(10);
        Button addSegmentButton = new Button("Добавить тестовый отрезок");
        addSegmentButton.setOnAction(e -> {
            double randX = Math.random() * 200 - 100;
            double randY = Math.random() * 200 - 100;
            viewModel.addSegment(new Point(0, 0), new Point(randX, randY));
        });
        controls.getChildren().add(addSegmentButton);

        // 5. Компоновка UI
        StackPane canvasPane = new StackPane(canvas);
        BorderPane root = new BorderPane();
        root.setCenter(canvasPane);
        root.setLeft(controls);

        // Адаптация размера Canvas под размер окна
        canvasPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setWidth(newVal.doubleValue());
            redraw();
        });
        canvasPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setHeight(newVal.doubleValue());
            redraw();
        });

        // 6. Связываем ViewModel с Renderer
        // Когда список сегментов в ViewModel меняется, мы перерисовываем
        viewModel.segmentsProperty().addListener((obs, oldList, newList) -> redraw());
        viewModel.gridConfigProperty().addListener(obs -> redraw());

        // Первичная отрисовка
        Platform.runLater(this::redraw);

        Scene scene = new Scene(root, 1024, 768);
        stage.setScene(scene);
        stage.setTitle("JavaFX CAD System (macOS)");
        stage.show();
    }

    /**
     * Метод перерисовки.
     * Собирает текущее состояние из ViewModel и отправляет в Renderer.
     */
    private void redraw() {
        var context = new DrawingContext(
                viewModel.segmentsProperty().get(), // Берем сегменты из VM
                "#FFFFFF", // белый фон
                viewModel.gridConfigProperty().get().gridColor(),
                viewModel.gridConfigProperty().get().axisColor(),
                viewModel.gridConfigProperty().get().gridStep(),
                viewModel.gridConfigProperty().get().showGrid(),
                viewModel.gridConfigProperty().get().showAxis(),
                viewModel.lineStyleProperty().get()
        );
        renderer.render(context);
    }

    public static void main(String[] args) {
        launch();
    }
}