package com.cadsystem.ui.viewmodel;

import com.cadsystem.app.EventBus;
import com.cadsystem.domain.event.SegmentCreatedEvent;
import com.cadsystem.domain.event.SettingsChangedEvent;
import com.cadsystem.domain.model.*;
import com.cadsystem.domain.service.CoordinateTransformer;
import com.cadsystem.domain.service.GeometryCalculator;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * ViewModel для управления состоянием основного окна.
 */
public class MainViewModel {

    private static final Logger logger = LoggerFactory.getLogger(
            MainViewModel.class
    );

    private final EventBus eventBus;
    private final CoordinateTransformer coordinateTransformer;
    private final GeometryCalculator geometryCalculator;

    // --- Состояние Модели (Данные) ---
    private final ListProperty<Segment> segments =
            new SimpleListProperty<>(FXCollections.observableArrayList());

    private final SimpleObjectProperty<Segment> selectedSegment =
            new SimpleObjectProperty<>();

    // --- Состояние Настроек (Конфигурация) ---
    private final SimpleObjectProperty<CoordinateSystem> coordinateSystem =
            new SimpleObjectProperty<>(CoordinateSystem.CARTESIAN);

    private final SimpleObjectProperty<AngleUnit> angleUnit =
            new SimpleObjectProperty<>(AngleUnit.DEGREES);

    private final SimpleDoubleProperty gridStep =
            new SimpleDoubleProperty(GridConfig.DEFAULT.gridStep());

    private final SimpleObjectProperty<GridConfig> gridConfig =
            new SimpleObjectProperty<>(GridConfig.DEFAULT);

    private final ObjectProperty<LineStyle> lineStyle =
            new SimpleObjectProperty<>(LineStyle.DEFAULT);

    // Новые свойства для цветов
    private final StringProperty segmentColor = new SimpleStringProperty("#000000"); // Черный по умолчанию
    private final StringProperty backgroundColor = new SimpleStringProperty("#FFFFFF"); // Белый по умолчанию
    private final StringProperty gridColor = new SimpleStringProperty("#CCCCCC");     // Серый по умолчанию

    // --- Свойства для ввода координат ---
    private final DoubleProperty x1 = new SimpleDoubleProperty(0.0);
    private final DoubleProperty y1 = new SimpleDoubleProperty(0.0);
    private final DoubleProperty x2 = new SimpleDoubleProperty(100.0);
    private final DoubleProperty y2 = new SimpleDoubleProperty(100.0);

    private final DoubleProperty r1 = new SimpleDoubleProperty(0.0);
    private final DoubleProperty theta1 = new SimpleDoubleProperty(0.0);
    private final DoubleProperty r2 = new SimpleDoubleProperty(141.4);
    private final DoubleProperty theta2 = new SimpleDoubleProperty(45.0);

    // --- Свойства для информационной панели ---
    private final StringProperty startPointInfo = new SimpleStringProperty("-");
    private final StringProperty endPointInfo = new SimpleStringProperty("-");
    private boolean isUpdatingFromCode = false;
    private final StringProperty segmentLengthInfo = new SimpleStringProperty("-");
    private final StringProperty segmentAngleInfo = new SimpleStringProperty("-");


    public MainViewModel(
            EventBus eventBus,
            CoordinateTransformer coordinateTransformer,
            GeometryCalculator geometryCalculator
    ) {
        this.eventBus = Objects.requireNonNull(eventBus);
        this.coordinateTransformer = Objects.requireNonNull(
                coordinateTransformer
        );
        this.geometryCalculator = Objects.requireNonNull(geometryCalculator);

        subscribeToEvents();

        // Обновляем инфо-панель при изменении текущего сегмента или настроек
        selectedSegment.addListener(obs -> updateSegmentInfo(selectedSegment.get()));
        coordinateSystem.addListener(obs -> updateSegmentInfo(selectedSegment.get()));
        angleUnit.addListener(obs -> updateSegmentInfo(selectedSegment.get()));

        setupCoordinateListeners();

        logger.info("MainViewModel инициализирован");
    }

    // --- Действия (Actions) ---

    public void addSegment(Point start, Point end) {
        try {
            var segment = new Segment(start, end);
            segments.add(segment);
            selectedSegment.set(segment);

            // Публикуем событие
            eventBus.publish(
                    new SegmentCreatedEvent(segment, coordinateSystem.get())
            );

            logger.info("Отрезок добавлен: {}", segment);
        } catch (Exception e) {
            logger.error("Ошибка добавления отрезка", e);
            // Здесь можно отправить собые UI_ERROR
        }
    }

    public void buildSegmentFromTextFields() {
        Point start, end;
        if (coordinateSystem.get() == CoordinateSystem.CARTESIAN) {
            start = new Point(x1.get(), y1.get());
            end = new Point(x2.get(), y2.get());
        } else {
            start = coordinateTransformer.toCartesian(new Point(r1.get(), theta1.get()), angleUnit.get());
            end = coordinateTransformer.toCartesian(new Point(r2.get(), theta2.get()), angleUnit.get());
        }
        addSegment(start, end);
    }

    public void selectSegmentAt(Point clickPoint) {
        Segment closestSegment = null;
        double minDistance = Double.MAX_VALUE;

        for (Segment segment : segments) {
            double distance = geometryCalculator.distanceToPoint(segment, clickPoint);
            if (distance < minDistance) {
                minDistance = distance;
                closestSegment = segment;
            }
        }

        // Порог для выбора (например, 10 пикселей)
        double selectionTolerance = 10.0;
        if (closestSegment != null && minDistance < selectionTolerance) {
            selectedSegment.set(closestSegment);
            logger.info("Выбран отрезок: {}", closestSegment);
        } else {
            selectedSegment.set(null);
            logger.info("Ни один отрезок не выбран");
        }
    }

    public void clearCurrentSegment() {
        selectedSegment.set(null);
        logger.info("Текущий отрезок очищен");
    }

    public void deleteCurrentSegment() {
        Segment current = selectedSegment.get();
        if (current != null) {
            segments.remove(current);
            selectedSegment.set(null);
            logger.info("Отрезок удалён");
        }
    }

    public void setGridStep(double step) {
        if (step <= 0) {
            throw new IllegalArgumentException(
                    "Шаг сетки должен быть > 0"
            );
        }

        eventBus.publish(
                new SettingsChangedEvent("gridStep", gridStep.get(), step)
        );

        gridStep.set(step);
        // Обновляем GridConfig
        gridConfig.set(new GridConfig(
                step,
                gridConfig.get().gridColor(),
                gridConfig.get().showGrid(),
                gridConfig.get().axisColor(),
                gridConfig.get().showAxis()
        ));
    }

    private void updateSegmentInfo(Segment segment) {
        if (segment == null) {
            startPointInfo.set("-");
            endPointInfo.set("-");
            segmentLengthInfo.set("-");
            segmentAngleInfo.set("-");
            return;
        }

        Point start = segment.start();
        Point end = segment.end();

        // Форматирование координат в зависимости от выбранной системы
        String startStr, endStr;
        if (coordinateSystem.get() == CoordinateSystem.CARTESIAN) {
            startStr = String.format("x: %.2f, y: %.2f", start.x(), start.y());
            endStr = String.format("x: %.2f, y: %.2f", end.x(), end.y());
        } else {
            Point startPolar = coordinateTransformer.toPolar(start, angleUnit.get());
            Point endPolar = coordinateTransformer.toPolar(end, angleUnit.get());
            startStr = String.format("r: %.2f, θ: %.2f", startPolar.x(), startPolar.y());
            endStr = String.format("r: %.2f, θ: %.2f", endPolar.x(), endPolar.y());
        }

        startPointInfo.set(startStr);
        endPointInfo.set(endStr);
        segmentLengthInfo.set(String.format("%.2f", segment.length()));

        // Угол наклона
        double angle = segment.angleRadians(); // в радианах
        if (angleUnit.get() == AngleUnit.DEGREES) {
            angle = Math.toDegrees(angle);
        }
        segmentAngleInfo.set(String.format("%.2f", angle));
    }

    private void setupCoordinateListeners() {
        x1.addListener((obs, oldV, newV) -> handleCartesianUpdate(
                () -> {
                    Point polar = coordinateTransformer.toPolar(
                            new Point(newV.doubleValue(), y1.get()), angleUnit.get());
                    r1.set(polar.x());
                    theta1.set(polar.y());
                }
        ));
        y1.addListener((obs, oldV, newV) -> handleCartesianUpdate(
                () -> {
                    Point polar = coordinateTransformer.toPolar(
                            new Point(x1.get(), newV.doubleValue()), angleUnit.get());
                    r1.set(polar.x());
                    theta1.set(polar.y());
                }
        ));
        x2.addListener((obs, oldV, newV) -> handleCartesianUpdate(
                () -> {
                    Point polar = coordinateTransformer.toPolar(
                            new Point(newV.doubleValue(), y2.get()), angleUnit.get());
                    r2.set(polar.x());
                    theta2.set(polar.y());
                }
        ));
        y2.addListener((obs, oldV, newV) -> handleCartesianUpdate(
                () -> {
                    Point polar = coordinateTransformer.toPolar(
                            new Point(x2.get(), newV.doubleValue()), angleUnit.get());
                    r2.set(polar.x());
                    theta2.set(polar.y());
                }
        ));

        r1.addListener((obs, oldV, newV) -> handlePolarUpdate(
                () -> {
                    Point cartesian = coordinateTransformer.toCartesian(
                            new Point(newV.doubleValue(), theta1.get()), angleUnit.get());
                    x1.set(cartesian.x());
                    y1.set(cartesian.y());
                }
        ));
        theta1.addListener((obs, oldV, newV) -> handlePolarUpdate(
                () -> {
                    Point cartesian = coordinateTransformer.toCartesian(
                            new Point(r1.get(), newV.doubleValue()), angleUnit.get());
                    x1.set(cartesian.x());
                    y1.set(cartesian.y());
                }
        ));
        r2.addListener((obs, oldV, newV) -> handlePolarUpdate(
                () -> {
                    Point cartesian = coordinateTransformer.toCartesian(
                            new Point(newV.doubleValue(), theta2.get()), angleUnit.get());
                    x2.set(cartesian.x());
                    y2.set(cartesian.y());
                }
        ));
        theta2.addListener((obs, oldV, newV) -> handlePolarUpdate(
                () -> {
                    Point cartesian = coordinateTransformer.toCartesian(
                            new Point(r2.get(), newV.doubleValue()), angleUnit.get());
                    x2.set(cartesian.x());
                    y2.set(cartesian.y());
                }
        ));
    }

    private void handleCartesianUpdate(Runnable updater) {
        if (isUpdatingFromCode) return;
        isUpdatingFromCode = true;
        updater.run();
        isUpdatingFromCode = false;
    }

    private void handlePolarUpdate(Runnable updater) {
        if (isUpdatingFromCode) return;
        isUpdatingFromCode = true;
        updater.run();
        isUpdatingFromCode = false;
    }

    private void subscribeToEvents() {
        // Слушаем изменения настроек от других компонентов (если они будут)
        eventBus.subscribe(SettingsChangedEvent.class, event -> {
            if (event.settingKey().equals("gridStep")) {
                double newStep = (Double) event.newValue();
                if (gridStep.get() != newStep) {
                    gridStep.set(newStep);
                }
            }
            logger.debug("Событие изменения настроек обработано: {}",
                    event.settingKey());
        });
    }

    // --- Getters для привязки в JavaFX (Properties) ---

    public ListProperty<Segment> segmentsProperty() {
        return segments;
    }

    public ObjectProperty<Segment> selectedSegmentProperty() {
        return selectedSegment;
    }

    public ObjectProperty<CoordinateSystem> coordinateSystemProperty() {
        return coordinateSystem;
    }

    public ObjectProperty<AngleUnit> angleUnitProperty() {
        return angleUnit;
    }

    public DoubleProperty gridStepProperty() {
        return gridStep;
    }

    public ReadOnlyObjectProperty<GridConfig> gridConfigProperty() {
        return gridConfig;
    }

    public ObjectProperty<LineStyle> lineStyleProperty() {
        return lineStyle;
    }
    public LineStyle getLineStyle() {
        return lineStyle.get();
    }
    public void setLineStyle(LineStyle style) {
        lineStyle.set(style);
    }

    // --- Getters для новых свойств ---
    public StringProperty segmentColorProperty() {
        return segmentColor;
    }

    public StringProperty backgroundColorProperty() {
        return backgroundColor;
    }

    public StringProperty gridColorProperty() {
        return gridColor;
    }

    // --- Getters для свойств координат ---
    public DoubleProperty x1Property() { return x1; }
    public DoubleProperty y1Property() { return y1; }
    public DoubleProperty x2Property() { return x2; }
    public DoubleProperty y2Property() { return y2; }
    public DoubleProperty r1Property() { return r1; }
    public DoubleProperty theta1Property() { return theta1; }
    public DoubleProperty r2Property() { return r2; }
    public DoubleProperty theta2Property() { return theta2; }

    // --- Getters для инфо-панели ---
    public StringProperty startPointInfoProperty() { return startPointInfo; }
    public StringProperty endPointInfoProperty() { return endPointInfo; }
    public StringProperty segmentLengthInfoProperty() { return segmentLengthInfo; }
    public StringProperty segmentAngleInfoProperty() { return segmentAngleInfo; }
}