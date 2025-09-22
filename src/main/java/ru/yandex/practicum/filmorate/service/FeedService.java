package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FeedRepository;
import ru.yandex.practicum.filmorate.dto.feed.FeedEventDto;
import ru.yandex.practicum.filmorate.mapper.FeedEventMapper;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.FeedEvent;
import ru.yandex.practicum.filmorate.model.feed.Operation;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;

    public enum Scope { OWN, FRIENDS, OWN_AND_FRIENDS }

    public void recordEvent(Long userId, EventType type, Operation op, Long entityId) {
        feedRepository.save(userId, type, op, entityId, System.currentTimeMillis());
    }

    public List<FeedEventDto> getUserFeed(Long userId, String scope) {
        Scope sc = Scope.valueOf(scope.toUpperCase());
        List<FeedEvent> events = switch (sc) {
            case OWN -> feedRepository.findByUserIdOrderByTimestamp(userId);
            case FRIENDS -> feedRepository.findFriendsFeedByUserIdOrderByTimestamp(userId);
            case OWN_AND_FRIENDS -> feedRepository.findOwnAndFriendsFeedByUserIdOrderByTimestamp(userId);
        };
        return events.stream()
                .map(FeedEventMapper::toDto)
                .toList();
    }
}