package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class EventRowMapper implements RowMapper<Event> {

    @Override
    public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Event.builder()
                .id(rs.getLong("id"))
                .idEntity(rs.getLong("idEntity"))
                .idUser(rs.getLong("idUser"))
                .eventType(EventType.valueOf(rs.getString("eventType")))
                .operation(Operation.valueOf(rs.getString("operation")))
                .timeCreated(rs.getTimestamp("timeCreated"))
                .build();
    }
}
