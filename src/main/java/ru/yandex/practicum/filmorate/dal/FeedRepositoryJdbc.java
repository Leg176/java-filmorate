package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.FeedEvent;
import ru.yandex.practicum.filmorate.model.feed.Operation;

import java.sql.Timestamp;
import java.util.List;

@Slf4j
@Repository
public class FeedRepositoryJdbc extends BaseRepository<FeedEvent> implements FeedRepository {

    private static final String INSERT_SQL =
            "INSERT INTO FEED_EVENTS (ts, idUser, eventType, operation, entityId) VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_OWN =
            "SELECT idEvent, ts, idUser, eventType, operation, entityId " +
                    "FROM FEED_EVENTS WHERE idUser = ? ORDER BY ts ASC, idEvent ASC";

    private static final String SELECT_FRIENDS =
            "SELECT e.idEvent, e.ts, e.idUser, e.eventType, e.operation, e.entityId " +
                    "FROM FEED_EVENTS e " +
                    "JOIN Friends f ON f.idUserFriends = e.idUser " +
                    "WHERE f.idUser = ? ORDER BY e.ts ASC, e.idEvent ASC";

    private static final String SELECT_OWN_AND_FRIENDS =
            "SELECT idEvent, ts, idUser, eventType, operation, entityId FROM (" +
                    "  SELECT e.idEvent, e.ts, e.idUser, e.eventType, e.operation, e.entityId " +
                    "  FROM FEED_EVENTS e WHERE e.idUser = ? " +
                    "  UNION ALL " +
                    "  SELECT e2.idEvent, e2.ts, e2.idUser, e2.eventType, e2.operation, e2.entityId " +
                    "  FROM FEED_EVENTS e2 JOIN Friends f ON f.idUserFriends = e2.idUser " +
                    "  WHERE f.idUser = ? " +
                    ") t ORDER BY ts ASC, idEvent ASC";

    public FeedRepositoryJdbc(JdbcTemplate jdbc, RowMapper<FeedEvent> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public FeedEvent save(Long userId, EventType eventType, Operation operation, Long entityId, Long timestampMillis) {
        long id = insert(
                INSERT_SQL,
                "idEvent",
                new Timestamp(timestampMillis),
                userId,
                eventType.name(),
                operation.name(),
                entityId
        );
        FeedEvent created = new FeedEvent(id, timestampMillis, userId, eventType, operation, entityId);
        log.debug("Saved feed event: {}", created);
        return created;
    }

    @Override
    public List<FeedEvent> findByUserIdOrderByTimestamp(Long userId) {
        return findMany(SELECT_OWN, userId);
    }

    @Override
    public List<FeedEvent> findFriendsFeedByUserIdOrderByTimestamp(Long userId) {
        return findMany(SELECT_FRIENDS, userId);
    }

    @Override
    public List<FeedEvent> findOwnAndFriendsFeedByUserIdOrderByTimestamp(Long userId) {
        return findMany(SELECT_OWN_AND_FRIENDS, userId, userId);
    }
}