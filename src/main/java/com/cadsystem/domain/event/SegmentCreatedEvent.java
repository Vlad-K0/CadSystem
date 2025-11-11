package com.cadsystem.domain.event;

import com.cadsystem.domain.model.CoordinateSystem;
import com.cadsystem.domain.model.Segment;
import java.time.Instant;

public record SegmentCreatedEvent(
        String eventType,
        Instant timestamp,
        Segment segment,
        CoordinateSystem coordinateSystem
) implements DomainEvent {

    public SegmentCreatedEvent(Segment segment, CoordinateSystem system) {
        this("segment:created", Instant.now(), segment, system);
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