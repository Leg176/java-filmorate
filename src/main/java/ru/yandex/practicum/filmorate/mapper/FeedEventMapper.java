package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.feed.FeedEventDto;
import ru.yandex.practicum.filmorate.model.feed.FeedEvent;

import java.util.List;

public final class FeedEventMapper {

    private FeedEventMapper() {
    }

    public static FeedEventDto toDto(FeedEvent e) {
        return new FeedEventDto(
                e.getTimestamp(),
                e.getUserId(),
                e.getEventType(),
                e.getOperation(),
                e.getEventId(),
                e.getEntityId()
        );
    }

    public static List<FeedEventDto> toDto(List<FeedEvent> events) {
        return events.stream()
                .map(FeedEventMapper::toDto)
                .toList();
    }
}