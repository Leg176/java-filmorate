package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FeedRepository;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.FeedEvent;
import ru.yandex.practicum.filmorate.model.feed.Operation;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;

    public void recordEvent(Long userId, EventType type, Operation op, Long entityId) {
        feedRepository.save(userId, type, op, entityId, Instant.now().toEpochMilli());
    }

    public enum Scope { OWN, FRIENDS, OWN_AND_FRIENDS }

    public List<FeedEvent> getUserFeed(Long userId, Scope scope) {
        return switch (scope) {
            case OWN -> feedRepository.findByUserIdOrderByTimestamp(userId);
            case FRIENDS -> feedRepository.findFriendsFeedByUserIdOrderByTimestamp(userId);
            case OWN_AND_FRIENDS -> feedRepository.findOwnAndFriendsFeedByUserIdOrderByTimestamp(userId);
        };
    }
}
