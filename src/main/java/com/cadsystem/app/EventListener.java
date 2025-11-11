package com.cadsystem.app;

import com.cadsystem.domain.event.DomainEvent;

/**
 * Интерфейс для слушателей событий.
 */
@FunctionalInterface
public interface EventListener<T extends DomainEvent> {
    void onEvent(T event);
}