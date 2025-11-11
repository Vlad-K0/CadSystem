package com.cadsystem.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Простой DI контейнер для управления зависимостями.
 */
public class DependencyInjectionContainer {

    private static final Logger logger = LoggerFactory.getLogger(
            DependencyInjectionContainer.class
    );

    private final Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();
    private final Map<Class<?>, Supplier<?>> factories = new HashMap<>();

    /**
     * Регистрирует singleton-сервис.
     */
    public <T> void bindSingleton(Class<T> type, T instance) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(instance);

        singletons.put(type, instance);
        logger.debug("Зарегистрирован singleton: {}", type.getName());
    }

    /**
     * Регистрирует фабрику для создания экземпляров.
     */
    public <T> void bindFactory(Class<T> type, Supplier<T> factory) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(factory);

        factories.put(type, factory);
        logger.debug("Зарегистрирована фабрика: {}", type.getName());
    }

    /**
     * Получает или создаёт экземпляр сервиса.
     */
    @SuppressWarnings("unchecked")
    public <T> T resolve(Class<T> type) {
        // Сначала проверяем сingletons
        Object singleton = singletons.get(type);
        if (singleton != null) {
            return (T) singleton;
        }

        // Затем проверяем фабрики
        Supplier<?> factory = factories.get(type);
        if (factory != null) {
            return (T) factory.get();
        }

        throw new IllegalArgumentException(
                "Тип " + type.getName() + " не зарегистрирован в контейнере"
        );
    }
}