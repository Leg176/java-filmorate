package ru.yandex.practicum.filmorate.model.feed;

import java.util.Objects;

public class FeedEvent {

    private Long eventId;
    private Long timestamp;
    private Long userId;
    private EventType eventType;
    private Operation operation;
    private Long entityId;

    public FeedEvent() {
    }

    public FeedEvent(Long eventId, Long timestamp, Long userId,
                     EventType eventType, Operation operation, Long entityId) {
        this.eventId = eventId;
        this.timestamp = timestamp;
        this.userId = userId;
        this.eventType = eventType;
        this.operation = operation;
        this.entityId = entityId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FeedEvent)) return false;
        FeedEvent feedEvent = (FeedEvent) o;
        return Objects.equals(eventId, feedEvent.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "FeedEvent{" +
                "eventId=" + eventId +
                ", timestamp=" + timestamp +
                ", userId=" + userId +
                ", eventType=" + eventType +
                ", operation=" + operation +
                ", entityId=" + entityId +
                '}';
    }
}

