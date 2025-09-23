package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.event.EventResponseDto;
import ru.yandex.practicum.filmorate.model.Event;

public class EventMapper {

    public static EventResponseDto toDto(Event event) {
        return EventResponseDto.builder()
                .eventId(event.getId())
                .entityId(event.getIdEntity())
                .userId(event.getIdUser())
                .eventType(event.getEventType().name())
                .operation(event.getOperation().name())
                .timestamp(event.getTimeCreated().toInstant().toEpochMilli())
                .build();
    }
}
