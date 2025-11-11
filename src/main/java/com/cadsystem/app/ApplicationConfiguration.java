package com.cadsystem.app;

import com.cadsystem.domain.event.SegmentCreatedEvent;
import com.cadsystem.domain.event.SettingsChangedEvent;
import com.cadsystem.domain.service.CoordinateTransformer;
import com.cadsystem.domain.service.GeometryCalculator;
import com.cadsystem.domain.service.StandardCoordinateTransformer;
import com.cadsystem.graphics.render.JavaFXRenderer;
import com.cadsystem.graphics.render.Renderer;
import com.cadsystem.ui.viewmodel.MainViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Конфигурация приложения и инициализация всех компонентов.
 */
public class ApplicationConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(
            ApplicationConfiguration.class
    );

    /**
     * Создаёт и конфигурирует контейнер DI.
     * Мы не можем регистрировать Renderer здесь, так как ему нужен Canvas,
     * который создается в App.java.
     */
    public static DependencyInjectionContainer createDIContainer() {
        var container = new DependencyInjectionContainer();

        logger.info("Инициализация DI контейнера...");

        // Event Bus (Singleton)
        var eventBus = new EventBus();
        container.bindSingleton(EventBus.class, eventBus);

        // Domain Services
        var coordinateTransformer = new StandardCoordinateTransformer();
        container.bindSingleton(
                CoordinateTransformer.class,
                coordinateTransformer
        );

        var geometryCalculator = new GeometryCalculator();
        container.bindSingleton(
                GeometryCalculator.class,
                geometryCalculator
        );

        // UI Layer ViewModels
        container.bindFactory(
                MainViewModel.class,
                () -> new MainViewModel(
                        container.resolve(EventBus.class),
                        container.resolve(CoordinateTransformer.class),
                        container.resolve(GeometryCalculator.class)
                )
        );

        logger.info("DI контейнер успешно инициализирован");

        return container;
    }

    /**
     * Подписывает компоненты на события.
     */
    public static void configureEventSubscriptions(
            DependencyInjectionContainer container
    ) {
        var eventBus = container.resolve(EventBus.class);

        // Подписываем слушателей на события
        eventBus.subscribe(SegmentCreatedEvent.class, event -> {
            logger.info("Отрезок создан: {}", event.segment());
        });

        eventBus.subscribe(SettingsChangedEvent.class, event -> {
            logger.info("Настройки изменены: {} = {}",
                    event.settingKey(), event.newValue());
        });

        logger.info("Подписки на события успешно конфигурированы");
    }
}