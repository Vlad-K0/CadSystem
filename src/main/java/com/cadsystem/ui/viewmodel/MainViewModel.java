package com.cadsystem.ui.viewmodel;

import com.cadsystem.app.EventBus;
import com.cadsystem.domain.event.SegmentCreatedEvent;
import com.cadsystem.domain.event.SettingsChangedEvent;
import com.cadsystem.domain.model.*;
import com.cadsystem.domain.service.CoordinateTransformer;
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

    // --- Состояние Модели (Данные) ---
    private final ListProperty<Segment> segments =
            new SimpleListProperty<>(FXCollections.observableArrayList());

    private final SimpleObjectProperty<Segment> currentSegment =
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

    private final SimpleObjectProperty<LineStyle> lineStyle =
            new SimpleObjectProperty<>(LineStyle.DEFAULT);


    public MainViewModel(
            EventBus eventBus,
            CoordinateTransformer coordinateTransformer
    ) {
        this.eventBus = Objects.requireNonNull(eventBus);
        this.coordinateTransformer = Objects.requireNonNull(
                coordinateTransformer
        );

        subscribeToEvents();
        logger.info("MainViewModel инициализирован");
    }

    // --- Действия (Actions) ---

    public void addSegment(Point start, Point end) {
        try {
            var segment = new Segment(start, end);
            segments.add(segment);
            currentSegment.set(segment);

            // Публикуем событие
            eventBus.publish(
                    new SegmentCreatedEvent(segment, coordinateSystem.get())
            );

            logger.info("Отрезок добавлен: {}", segment);
        } catch (Exception e) {
            logger.error("Ошибка добавления отрезка", e);
            // Здесь можно отправить событие UI_ERROR
        }
    }

    public void deleteCurrentSegment() {
        Segment current = currentSegment.get();
        if (current != null) {
            segments.remove(current);
            currentSegment.set(null);
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

    public ObjectProperty<Segment> currentSegmentProperty() {
        return currentSegment;
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

    public ReadOnlyObjectProperty<LineStyle> lineStyleProperty() {
        return lineStyle;
    }
}