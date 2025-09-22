package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ReviewRowMapper implements RowMapper<Review> {
    @Override
    public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
        Review r = new Review();
        r.setIdReview(rs.getLong("reviewId"));
        r.setContent(rs.getString("content"));
        r.setIsPositive(rs.getBoolean("isPositive"));
        r.setUserId(rs.getLong("userId"));
        r.setFilmId(rs.getLong("filmId"));
        int useful = rs.getObject("useful") == null ? 0 : rs.getInt("useful");
        r.setUseful(useful);
        return r;
    }
}