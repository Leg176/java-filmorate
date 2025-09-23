package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;

import java.sql.Timestamp;

@Data
@Builder
public class Event {
    private Long id;
    private Long idEntity;
    private Long idUser;
    private EventType eventType;
    private Operation operation;
    private Timestamp timeCreated;
}
