package com.cadsystem;

import com.cadsystem.app.ApplicationConfiguration;
import com.cadsystem.app.DependencyInjectionContainer;
import com.cadsystem.app.EventBus;
import com.cadsystem.domain.model.*;
import com.cadsystem.graphics.render.DrawingContext;
import com.cadsystem.graphics.render.JavaFXRenderer;
import com.cadsystem.graphics.render.Renderer;
import com.cadsystem.ui.viewmodel.MainViewModel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

import java.util.List;

/**
 * JavaFX App
 */
public class App extends Application {

    private DependencyInjectionContainer container;
    private MainViewModel viewModel;
    private Renderer renderer;
    private Canvas canvas;
    private MatrixTransform matrixTransform = new MatrixTransform();
    private Label statusBar;

    private enum DrawingState {
        IDLE,
        WAITING_FOR_START_POINT,
        WAITING_FOR_END_POINT
    }
    private DrawingState drawingState = DrawingState.IDLE;

    private double lastMouseX = 0;
    private double lastMouseY = 0;

    @Override
    public void start(Stage stage) {
        // ... (инициализация как раньше) ...
        container = ApplicationConfiguration.createDIContainer();
        ApplicationConfiguration.configureEventSubscriptions(container);
        viewModel = container.resolve(MainViewModel.class);
        canvas = new Canvas(800, 600);
        renderer = new JavaFXRenderer(canvas, container.resolve(EventBus.class));
        statusBar = new Label("Ready");

        // --- Панель инструментов ---
        Button newSegmentButton = new Button("Отрезок");
        newSegmentButton.setOnAction(e -> {
            viewModel.clearCurrentSegment();
            drawingState = DrawingState.WAITING_FOR_START_POINT;
        });
        Button deleteSegmentButton = new Button("Удалить");
        deleteSegmentButton.setOnAction(e -> viewModel.deleteCurrentSegment());

        Button rotateLeftBtn = new Button("Rotate Left");
        rotateLeftBtn.setOnAction(e -> {
            matrixTransform.rotate(-Math.toRadians(15), 0, 0);
            redraw();
        });

        Button rotateRightBtn = new Button("Rotate Right");
        rotateRightBtn.setOnAction(e -> {
            matrixTransform.rotate(Math.toRadians(15), 0, 0);
            redraw();
        });

        Button resetViewBtn = new Button("Reset View");
        resetViewBtn.setOnAction(e -> {
            resetView();
            redraw();
        });

        ToolBar toolBar = new ToolBar(newSegmentButton, deleteSegmentButton, rotateLeftBtn, rotateRightBtn, resetViewBtn);

        // --- Остальной UI ---
        VBox leftPanel = new VBox(20);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 1 0 0;");
        leftPanel.setMinWidth(280);

        // ... (весь код leftPanel без изменений) ...
        Label settingsTitle = new Label("Настройки");
        settingsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        GridPane settingsGrid = new GridPane();
        settingsGrid.setHgap(10);
        settingsGrid.setVgap(10);
        Label coordSystemLabel = new Label("Система координат:");
        ToggleGroup coordToggleGroup = new ToggleGroup();
        RadioButton cartesianRadio = new RadioButton("Декартова");
        cartesianRadio.setToggleGroup(coordToggleGroup);
        cartesianRadio.setSelected(true);
        RadioButton polarRadio = new RadioButton("Полярная");
        polarRadio.setToggleGroup(coordToggleGroup);
        HBox coordBox = new HBox(10, cartesianRadio, polarRadio);
        settingsGrid.add(coordSystemLabel, 0, 0);
        settingsGrid.add(coordBox, 1, 0);
        Label angleUnitLabel = new Label("Единицы углов:");
        ToggleGroup angleToggleGroup = new ToggleGroup();
        RadioButton degreesRadio = new RadioButton("Градусы");
        degreesRadio.setToggleGroup(angleToggleGroup);
        degreesRadio.setSelected(true);
        RadioButton radiansRadio = new RadioButton("Радианы");
        radiansRadio.setToggleGroup(angleToggleGroup);
        HBox angleBox = new HBox(10, degreesRadio, radiansRadio);
        settingsGrid.add(angleUnitLabel, 0, 1);
        settingsGrid.add(angleBox, 1, 1);
        Label gridStepLabel = new Label("Шаг сетки:");
        TextField gridStepField = new TextField("20.0");
        settingsGrid.add(gridStepLabel, 0, 2);
        settingsGrid.add(gridStepField, 1, 2);
        Label segmentColorLabel = new Label("Цвет отрезка:");
        ColorPicker segmentColorPicker = new ColorPicker();
        settingsGrid.add(segmentColorLabel, 0, 3);
        settingsGrid.add(segmentColorPicker, 1, 3);
        Label backgroundColorLabel = new Label("Цвет фона:");
        ColorPicker backgroundColorPicker = new ColorPicker();
        settingsGrid.add(backgroundColorLabel, 0, 4);
        settingsGrid.add(backgroundColorPicker, 1, 4);
        Label gridColorLabel = new Label("Цвет сетки:");
        ColorPicker gridColorPicker = new ColorPicker();
        settingsGrid.add(gridColorLabel, 0, 5);
        settingsGrid.add(gridColorPicker, 1, 5);
        Label coordsTitle = new Label("Координаты");
        coordsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        GridPane cartesianPane = new GridPane();
        cartesianPane.setHgap(10);
        cartesianPane.setVgap(5);
        TextField x1Field = new TextField("0.0");
        TextField y1Field = new TextField("0.0");
        TextField x2Field = new TextField("100.0");
        TextField y2Field = new TextField("100.0");
        cartesianPane.add(new Label("x1:"), 0, 0);
        cartesianPane.add(x1Field, 1, 0);
        cartesianPane.add(new Label("y1:"), 2, 0);
        cartesianPane.add(y1Field, 3, 0);
        cartesianPane.add(new Label("x2:"), 0, 1);
        cartesianPane.add(x2Field, 1, 1);
        cartesianPane.add(new Label("y2:"), 2, 1);
        cartesianPane.add(y2Field, 3, 1);
        GridPane polarPane = new GridPane();
        polarPane.setHgap(10);
        polarPane.setVgap(5);
        TextField r1Field = new TextField("0.0");
        TextField theta1Field = new TextField("0.0");
        TextField r2Field = new TextField("141.4");
        TextField theta2Field = new TextField("45.0");
        polarPane.add(new Label("r1:"), 0, 0);
        polarPane.add(r1Field, 1, 0);
        polarPane.add(new Label("θ1:"), 2, 0);
        polarPane.add(theta1Field, 3, 0);
        polarPane.add(new Label("r2:"), 0, 1);
        polarPane.add(r2Field, 1, 1);
        polarPane.add(new Label("θ2:"), 2, 1);
        polarPane.add(theta2Field, 3, 1);
        polarPane.setVisible(false);
        Button buildSegmentButton = new Button("Построить отрезок");
        HBox buildButtonBox = new HBox(buildSegmentButton);
        buildButtonBox.setPadding(new Insets(10, 0, 0, 0));
        Label infoTitle = new Label("Информация");
        infoTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(5);
        Label startPointLabel = new Label("Начало:");
        Label startPointValue = new Label("-");
        infoGrid.add(startPointLabel, 0, 0);
        infoGrid.add(startPointValue, 1, 0);
        Label endPointLabel = new Label("Конец:");
        Label endPointValue = new Label("-");
        infoGrid.add(endPointLabel, 0, 1);
        infoGrid.add(endPointValue, 1, 1);
        Label lengthLabel = new Label("Длина:");
        Label lengthValue = new Label("-");
        infoGrid.add(lengthLabel, 0, 2);
        infoGrid.add(lengthValue, 1, 2);
        Label angleLabel = new Label("Угол наклона:");
        Label angleValue = new Label("-");
        infoGrid.add(angleLabel, 0, 3);
        infoGrid.add(angleValue, 1, 3);
        leftPanel.getChildren().addAll(
                settingsTitle, settingsGrid,
                coordsTitle, cartesianPane, polarPane,
                buildButtonBox,
                new Separator(),
                infoTitle, infoGrid
        );

        // --- Компоновка ---
        StackPane canvasPane = new StackPane(canvas);
        BorderPane root = new BorderPane();
        root.setTop(toolBar);
        root.setCenter(canvasPane);
        root.setLeft(leftPanel);
        root.setBottom(statusBar);

        canvasPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setWidth(newVal.doubleValue());
            redraw();
        });
        canvasPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setHeight(newVal.doubleValue());
            redraw();
        });

        // --- Обработчики событий ---
        canvas.setOnMouseClicked(this::handleCanvasClick);
        canvas.setOnScroll(this::handleCanvasScroll);
        canvas.setOnMousePressed(event -> {
            if (event.isMiddleButtonDown()) {
                lastMouseX = event.getX();
                lastMouseY = event.getY();
            }
        });
        canvas.setOnMouseDragged(event -> {
            if (event.isMiddleButtonDown()) {
                double dx = event.getX() - lastMouseX;
                double dy = event.getY() - lastMouseY;

                try {
                    AffineTransform inverse = matrixTransform.getTransform().createInverse();
                    Point2D.Double delta = new Point2D.Double(dx, dy);
                    inverse.deltaTransform(delta, delta);
                    matrixTransform.pan(delta.x, delta.y);
                } catch (NoninvertibleTransformException e) {
                    e.printStackTrace();
                }

                lastMouseX = event.getX();
                lastMouseY = event.getY();
                redraw();
            }
        });
        canvas.setOnMouseMoved(e -> updateStatus(e.getX(), e.getY()));

        canvas.setFocusTraversable(true);
        canvas.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT) {
                matrixTransform.rotate(-Math.toRadians(event.isShiftDown() ? 90 : 15), 0, 0);
                redraw();
            } else if (event.getCode() == KeyCode.RIGHT) {
                matrixTransform.rotate(Math.toRadians(event.isShiftDown() ? 90 : 15), 0, 0);
                redraw();
            }
        });

        // ... (bindViewModel и остальные методы без изменений) ...
        bindViewModel(cartesianRadio, polarRadio, degreesRadio, radiansRadio, gridStepField, segmentColorPicker, backgroundColorPicker, gridColorPicker, cartesianPane, polarPane, x1Field, y1Field, x2Field, y2Field, r1Field, theta1Field, r2Field, theta2Field, startPointValue, endPointValue, lengthValue, angleValue);
        buildSegmentButton.setOnAction(e -> viewModel.buildSegmentFromTextFields());
        viewModel.selectedSegmentProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection == null) {
                buildSegmentButton.setText("Построить отрезок");
                buildSegmentButton.setOnAction(e -> viewModel.buildSegmentFromTextFields());
            } else {
                buildSegmentButton.setText("Применить изменения");
                buildSegmentButton.setOnAction(e -> viewModel.updateSelectedSegment());
            }
        });
        viewModel.segmentsProperty().addListener((obs, oldList, newList) -> redraw());
        viewModel.selectedSegmentProperty().addListener(obs -> redraw());
        viewModel.gridConfigProperty().addListener(obs -> redraw());
        viewModel.backgroundColorProperty().addListener(obs -> redraw());
        viewModel.lineStyleProperty().addListener(obs -> redraw());
        viewModel.segmentColorProperty().addListener(obs -> {
            LineStyle oldStyle = viewModel.getLineStyle();
            if (oldStyle != null) {
                viewModel.setLineStyle(new LineStyle(
                        viewModel.segmentColorProperty().get(),
                        oldStyle.thickness(),
                        oldStyle.strokeType()
                ));
            }
        });

        // --- Запуск ---
        Platform.runLater(this::resetView);
        Scene scene = new Scene(root, 1024, 768);
        stage.setScene(scene);
        stage.setTitle("JavaFX CAD System (macOS)");
        stage.show();
        canvas.requestFocus();
    }

    private void resetView() {
        matrixTransform.reset();
        matrixTransform.pan(canvas.getWidth()/2, canvas.getHeight()/2);
        matrixTransform.getTransform().scale(1, -1); // Инвертируем Y
        redraw();
    }

    private void redraw() {
        var context = new DrawingContext(
                viewModel.segmentsProperty().get(),
                viewModel.selectedSegmentProperty().get(),
                viewModel.backgroundColorProperty().get(),
                viewModel.gridConfigProperty().get().gridColor(),
                viewModel.gridConfigProperty().get().axisColor(),
                viewModel.gridConfigProperty().get().gridStep(),
                viewModel.gridConfigProperty().get().showGrid(),
                viewModel.gridConfigProperty().get().showAxis(),
                matrixTransform.getTransform()
        );
        renderer.render(context);
    }

    private void updateStatus(double mouseX, double mouseY) {
        double scale = matrixTransform.getScale();
        double angle = Math.toDegrees(matrixTransform.getRotationAngle());
        Point2D worldCoords = matrixTransform.screenToWorld(new Point2D.Double(mouseX, mouseY));

        String status = String.format(
            "Scale: %.2f%% | Angle: %.1f° | Coords: (X: %.2f, Y: %.2f)",
            scale * 100, angle, worldCoords.getX(), worldCoords.getY()
        );
        statusBar.setText(status);
    }

    public static void main(String[] args) {
        launch();
    }

    private void handleCanvasScroll(ScrollEvent event) {
        double delta = 1.2;
        double scaleFactor = event.getDeltaY() > 0 ? delta : 1/delta;
        matrixTransform.zoom(scaleFactor, event.getX(), event.getY());
        redraw();
    }

    // ... (остальные методы без изменений) ...
    private void bindViewModel(RadioButton cartesianRadio, RadioButton polarRadio, RadioButton degreesRadio, RadioButton radiansRadio, TextField gridStepField, ColorPicker segmentColorPicker, ColorPicker backgroundColorPicker, ColorPicker gridColorPicker, GridPane cartesianPane, GridPane polarPane, TextField x1Field, TextField y1Field, TextField x2Field, TextField y2Field, TextField r1Field, TextField theta1Field, TextField r2Field, TextField theta2Field, Label startPointValue, Label endPointValue, Label lengthValue, Label angleValue) {
        viewModel.coordinateSystemProperty().addListener((obs, oldVal, newVal) -> {
            cartesianPane.setVisible(newVal == CoordinateSystem.CARTESIAN);
            polarPane.setVisible(newVal == CoordinateSystem.POLAR);
        });
        cartesianRadio.setOnAction(e -> viewModel.coordinateSystemProperty().set(CoordinateSystem.CARTESIAN));
        polarRadio.setOnAction(e -> viewModel.coordinateSystemProperty().set(CoordinateSystem.POLAR));
        degreesRadio.setOnAction(e -> viewModel.angleUnitProperty().set(AngleUnit.DEGREES));
        radiansRadio.setOnAction(e -> viewModel.angleUnitProperty().set(AngleUnit.RADIANS));
        StringConverter<Number> converter = new StringConverter<>() {
            @Override
            public String toString(Number object) {
                return object == null ? "" : object.toString();
            }
            @Override
            public Number fromString(String string) {
                try {
                    return Double.parseDouble(string);
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
        };
        gridStepField.textProperty().bindBidirectional(viewModel.gridStepProperty(), converter);
        x1Field.textProperty().bindBidirectional(viewModel.x1Property(), converter);
        y1Field.textProperty().bindBidirectional(viewModel.y1Property(), converter);
        x2Field.textProperty().bindBidirectional(viewModel.x2Property(), converter);
        y2Field.textProperty().bindBidirectional(viewModel.y2Property(), converter);
        r1Field.textProperty().bindBidirectional(viewModel.r1Property(), converter);
        theta1Field.textProperty().bindBidirectional(viewModel.theta1Property(), converter);
        r2Field.textProperty().bindBidirectional(viewModel.r2Property(), converter);
        theta2Field.textProperty().bindBidirectional(viewModel.theta2Property(), converter);
        segmentColorPicker.valueProperty().addListener((obs, oldColor, newColor) -> {
            viewModel.segmentColorProperty().set(colorToHexString(newColor));
        });
        backgroundColorPicker.valueProperty().addListener((obs, oldColor, newColor) -> {
            viewModel.backgroundColorProperty().set(colorToHexString(newColor));
        });
        gridColorPicker.valueProperty().addListener((obs, oldColor, newColor) -> {
            viewModel.gridColorProperty().set(colorToHexString(newColor));
        });
        segmentColorPicker.setValue(Color.web(viewModel.segmentColorProperty().get()));
        viewModel.segmentColorProperty().addListener((obs, oldC, newC) -> segmentColorPicker.setValue(Color.web(newC)));
        backgroundColorPicker.setValue(Color.web(viewModel.backgroundColorProperty().get()));
        gridColorPicker.setValue(Color.web(viewModel.gridColorProperty().get()));
        startPointValue.textProperty().bind(viewModel.startPointInfoProperty());
        endPointValue.textProperty().bind(viewModel.endPointInfoProperty());
        lengthValue.textProperty().bind(viewModel.segmentLengthInfoProperty());
        angleValue.textProperty().bind(viewModel.segmentAngleInfoProperty());
    }
    private String colorToHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
    private void handleCanvasClick(MouseEvent event) {
        Point2D.Double worldCoords = (Point2D.Double) matrixTransform.screenToWorld(new Point2D.Double(event.getX(), event.getY()));
        double coordX = worldCoords.getX();
        double coordY = worldCoords.getY();

        switch (drawingState) {
            case WAITING_FOR_START_POINT:
                viewModel.x1Property().set(coordX);
                viewModel.y1Property().set(coordY);
                drawingState = DrawingState.WAITING_FOR_END_POINT;
                break;
            case WAITING_FOR_END_POINT:
                viewModel.x2Property().set(coordX);
                viewModel.y2Property().set(coordY);
                viewModel.addSegment(
                        new Point(viewModel.x1Property().get(), viewModel.y1Property().get()),
                        new Point(coordX, coordY)
                );
                drawingState = DrawingState.IDLE;
                break;
            case IDLE:
                viewModel.selectSegmentAt(new Point(coordX, coordY));
                break;
        }
    }
}
