package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.FeedEvent;
import ru.yandex.practicum.filmorate.model.feed.Operation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Component
public class FeedEventRowMapper implements RowMapper<FeedEvent> {
    @Override
    public FeedEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
        Long id = rs.getLong("idEvent");
        Timestamp ts = rs.getTimestamp("ts");
        Long millis = ts == null ? null : ts.getTime();
        Long userId = rs.getLong("idUser");
        EventType type = EventType.valueOf(rs.getString("eventType"));
        Operation op = Operation.valueOf(rs.getString("operation"));
        Long entityId = rs.getLong("entityId");
        return new FeedEvent(id, millis, userId, type, op, entityId);
    }
}
