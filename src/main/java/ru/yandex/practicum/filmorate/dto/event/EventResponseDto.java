package ru.yandex.practicum.filmorate.dto.event;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class EventResponseDto {
    private Long eventId;
    private Long entityId;
    private String eventType;
    private String operation;
    private Long userId;
    private Long timestamp;
}
