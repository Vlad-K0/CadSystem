package com.cadsystem.app;

import com.cadsystem.domain.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Простая реализация Event Bus для декаплирования компонентов.
 * Использует Observer pattern.
 */
public class EventBus {

    private static final Logger logger = LoggerFactory.getLogger(
            EventBus.class
    );

    private final Map<Class<?>, List<EventListener<?>>> subscribers =
            new ConcurrentHashMap<>();

    /**
     * Подписывает слушателя на событие определённого типа.
     */
    @SuppressWarnings("unchecked")
    public <T extends DomainEvent> void subscribe(
            Class<T> eventType,
            EventListener<T> listener
    ) {
        Objects.requireNonNull(eventType, "Тип события не может быть null");
        Objects.requireNonNull(listener, "Слушатель не может быть null");

        subscribers
                .computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add((EventListener<?>) listener);

        logger.debug("Подписан слушатель на событие: {}", eventType.getName());
    }

    /**
     * Публикует событие всем заинтересованным подписчикам.
     */
    @SuppressWarnings("unchecked")
    public <T extends DomainEvent> void publish(T event) {
        Objects.requireNonNull(event, "Событие не может быть null");

        List<EventListener<?>> listeners = subscribers.get(event.getClass());

        if (listeners == null || listeners.isEmpty()) {
            logger.debug("Нет подписчиков на событие: {}",
                    event.getEventType());
            return;
        }

        logger.info("Публикуется событие '{}' для {} подписчиков",
                event.getEventType(), listeners.size());

        for (EventListener<?> listener : listeners) {
            try {
                ((EventListener<T>) listener).onEvent(event);
            } catch (Exception e) {
                logger.error("Ошибка при обработке события подписчиком", e);
                // Continue processing other listeners
            }
        }
    }

    /**
     * Отписывает слушателя от события.
     */
    public <T extends DomainEvent> void unsubscribe(
            Class<T> eventType,
            EventListener<T> listener
    ) {
        List<EventListener<?>> listeners = subscribers.get(eventType);
        if (listeners != null) {
            listeners.remove(listener);
            logger.debug("Отписан слушатель от события: {}", eventType.getName());
        }
    }
}