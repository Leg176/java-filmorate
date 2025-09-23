package ru.yandex.practicum.filmorate.model.feed;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedEvent {

    private Long eventId;

    private Long timestamp;

    private Long userId;

    private EventType eventType;

    private Operation operation;

    private Long entityId;
}