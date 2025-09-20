package ru.yandex.practicum.filmorate.dal;

import java.util.List;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.FeedEvent;
import ru.yandex.practicum.filmorate.model.feed.Operation;

public interface FeedRepository {

    FeedEvent save(Long userId, EventType eventType, Operation operation,
                   Long entityId, Long timestampMillis);

    List<FeedEvent> findByUserIdOrderByTimestamp(Long userId);

    List<FeedEvent> findFriendsFeedByUserIdOrderByTimestamp(Long userId);

    List<FeedEvent> findOwnAndFriendsFeedByUserIdOrderByTimestamp(Long userId);
}
