package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
@Component
public class ReviewsRowMapper implements RowMapper<Review> {

    @Override
    public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Review.builder()
                .id(rs.getLong("id"))
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("isPositive"))
                .userId(rs.getLong("idUser"))
                .filmId(rs.getLong("idFilm"))
                .useful(rs.getInt("useful"))
                .build();
    }
}
