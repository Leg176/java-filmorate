package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FeedRepository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;

import java.sql.Timestamp;
import java.time.Instant;

@Service
public class EventService {
    private final FeedRepository feedRepository;

    public EventService(FeedRepository feedRepository) {
        this.feedRepository = feedRepository;
    }

    public void add(Long idEntity, Long idUser, EventType eventType) {
        feedRepository.create(createEvent(idEntity, eventType, Operation.ADD, idUser));
    }

    public void delete(Long idEntity, Long idUser, EventType eventType) {
        feedRepository.create(createEvent(idEntity, eventType, Operation.REMOVE, idUser));
    }

    public void update(Long idEntity, Long idUser, EventType eventType) {
        feedRepository.create(createEvent(idEntity, eventType, Operation.UPDATE, idUser));
    }

    private Event createEvent(Long idEntity, EventType eventType, Operation operationType, Long idUser) {
        return Event.builder()
                .idEntity(idEntity)
                .eventType(eventType)
                .operation(operationType)
                .idUser(idUser)
                .timeCreated(Timestamp.from(Instant.now()))
                .build();
    }
}
