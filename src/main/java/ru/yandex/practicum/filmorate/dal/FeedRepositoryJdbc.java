package ru.yandex.practicum.filmorate.dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.FeedEvent;
import ru.yandex.practicum.filmorate.model.feed.Operation;

@Repository
public class FeedRepositoryJdbc implements FeedRepository {

    private static final Logger log = LoggerFactory.getLogger(FeedRepositoryJdbc.class);

    private final JdbcTemplate jdbcTemplate;

    public FeedRepositoryJdbc(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public FeedEvent save(Long userId, EventType eventType, Operation operation, Long entityId, Long timestampMillis) {
        final String sql = "INSERT INTO FEED_EVENTS (ts, idUser, eventType, operation, entityId) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"idEvent"});
            ps.setTimestamp(1, new Timestamp(timestampMillis));
            ps.setLong(2, userId);
            ps.setString(3, eventType.name());
            ps.setString(4, operation.name());
            ps.setLong(5, entityId);
            return ps;
        }, keyHolder);
        Long id = keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
        FeedEvent created = new FeedEvent(id, timestampMillis, userId, eventType, operation, entityId);
        log.debug("Saved feed event: {}", created);
        return created;
    }

    @Override
    public List<FeedEvent> findByUserIdOrderByTimestamp(Long userId) {
        final String sql = "SELECT idEvent, ts, idUser, eventType, operation, entityId " +
                "FROM FEED_EVENTS WHERE idUser = ? ORDER BY ts ASC, idEvent ASC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> map(rs), userId);
    }

    @Override
    public List<FeedEvent> findFriendsFeedByUserIdOrderByTimestamp(Long userId) {
        final String sql =
                "SELECT e.idEvent, e.ts, e.idUser, e.eventType, e.operation, e.entityId " +
                        "FROM FEED_EVENTS e " +
                        "JOIN Friends f ON f.idUserFriends = e.idUser " +
                        "WHERE f.idUser = ? " +
                        "ORDER BY e.ts ASC, e.idEvent ASC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> map(rs), userId);
    }

    @Override
    public List<FeedEvent> findOwnAndFriendsFeedByUserIdOrderByTimestamp(Long userId) {
        final String sql =
                "SELECT idEvent, ts, idUser, eventType, operation, entityId FROM (" +
                        "   SELECT e.idEvent, e.ts, e.idUser, e.eventType, e.operation, e.entityId " +
                        "   FROM FEED_EVENTS e WHERE e.idUser = ? " +
                        "   UNION ALL " +
                        "   SELECT e2.idEvent, e2.ts, e2.idUser, e2.eventType, e2.operation, e2.entityId " +
                        "   FROM FEED_EVENTS e2 " +
                        "   JOIN Friends f ON f.idUserFriends = e2.idUser " +
                        "   WHERE f.idUser = ? " +
                        ") t ORDER BY ts ASC, idEvent ASC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> map(rs), userId, userId);
    }

    private FeedEvent map(ResultSet rs) throws SQLException {
        Long id = rs.getLong("idEvent");
        Timestamp ts = rs.getTimestamp("ts");
        Long millis = ts.getTime();
        Long uid = rs.getLong("idUser");
        EventType type = EventType.valueOf(rs.getString("eventType"));
        Operation op = Operation.valueOf(rs.getString("operation"));
        Long entityId = rs.getLong("entityId");
        return new FeedEvent(id, millis, uid, type, op, entityId);
    }
}
