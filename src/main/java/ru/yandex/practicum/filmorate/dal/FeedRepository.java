package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

@Repository
public class FeedRepository extends BaseRepository<Event> {

    private static final String INSERT_QUERY =
            "INSERT INTO EVENTS (idEntity, eventType, operation, idUser, timeCreated) VALUES (?, ?, ?, ?, ?)";

    // порядок: новые сверху
    private static final String READ_EVENT_FEED_FOR_USER_QUERY =
            "SELECT * FROM EVENTS WHERE idUser = ? ORDER BY timeCreated DESC, id DESC";

    public FeedRepository(JdbcTemplate jdbc, RowMapper<Event> mapper) {
        super(jdbc, mapper);
    }

    public Event create(Event event) {
        long id = insert(
                INSERT_QUERY, "id",
                event.getIdEntity(),
                event.getEventType().name(),
                event.getOperation().name(),
                event.getIdUser(),
                event.getTimeCreated()
        );
        event.setId(id);
        return event;
    }

    public List<Event> getFeedForUser(Long id) {
        return jdbc.query(READ_EVENT_FEED_FOR_USER_QUERY, mapper, id);
    }
}