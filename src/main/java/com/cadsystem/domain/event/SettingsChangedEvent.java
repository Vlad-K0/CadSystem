package com.cadsystem.domain.event;

import java.time.Instant;

public record SettingsChangedEvent(
        String eventType,
        Instant timestamp,
        String settingKey,
        Object oldValue,
        Object newValue
) implements DomainEvent {

    public SettingsChangedEvent(String key, Object oldValue, Object newValue) {
        this("settings:changed", Instant.now(), key, oldValue, newValue);
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String getEventType() {
        return eventType;
    }
}