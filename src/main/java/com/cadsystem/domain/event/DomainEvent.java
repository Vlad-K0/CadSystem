package com.cadsystem.domain.event;

import java.time.Instant;

/**
 * Базовый интерфейс для доменных событий.
 */
public interface DomainEvent {
    /**
     * Уникальный тип события.
     */
    String getEventType();

    /**
     * Временная метка события.
     */
    Instant getTimestamp();
}