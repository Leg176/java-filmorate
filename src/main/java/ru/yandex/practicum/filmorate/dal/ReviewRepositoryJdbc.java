package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.dto.review.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.review.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryJdbc implements ReviewRepository {

    private final JdbcTemplate jdbc;
    private static final ReviewRowMapper ROW_MAPPER = new ReviewRowMapper();

    @Override
    public Review create(NewReviewRequest req) {
        final String sql = """
                INSERT INTO reviews (content, is_positive, user_id, film_id, useful)
                VALUES (?, ?, ?, ?, 0)
                """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, req.getContent());
            ps.setBoolean(2, req.getIsPositive());
            ps.setLong(3, req.getUserId());
            ps.setLong(4, req.getFilmId());
            return ps;
        }, kh);

        long id = kh.getKey().longValue();
        return getById(id).orElseThrow(() -> new NotFoundException("Не удалось прочитать созданный отзыв id=" + id));
    }

    @Override
    public Review update(UpdateReviewRequest req) {
        final String sql = """
                UPDATE reviews
                SET content = ?, is_positive = ?
                WHERE id_review = ?
                """;
        int updated = jdbc.update(sql, req.getContent(), req.getIsPositive(), req.getIdReview());
        if (updated == 0) {
            throw new NotFoundException("Отзыв не найден: id=" + req.getIdReview());
        }
        return getById(req.getIdReview()).orElseThrow(() -> new NotFoundException("Отзыв не найден после обновления"));
    }

    @Override
    public void delete(long reviewId) {
        int deleted = jdbc.update("DELETE FROM reviews WHERE id_review = ?", reviewId);
        if (deleted == 0) throw new NotFoundException("Отзыв не найден: id=" + reviewId);
    }

    @Override
    public Optional<Review> getById(long reviewId) {
        final String sql = """
                SELECT r.id_review              AS reviewId,
                       r.content                AS content,
                       r.is_positive            AS isPositive,
                       r.user_id                AS userId,
                       r.film_id                AS filmId,
                       COALESCE(r.useful, 0)    AS useful
                FROM reviews r
                WHERE r.id_review = ?
                """;
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, ROW_MAPPER, reviewId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Review> findByFilmId(long filmId, int count) {
        final String sql = """
                SELECT r.id_review              AS reviewId,
                       r.content                AS content,
                       r.is_positive            AS isPositive,
                       r.user_id                AS userId,
                       r.film_id                AS filmId,
                       COALESCE(r.useful, 0)    AS useful
                FROM reviews r
                WHERE r.film_id = ?
                ORDER BY useful DESC, reviewId ASC
                LIMIT ?
                """;
        return jdbc.query(sql, ROW_MAPPER, filmId, count);
    }

    @Override
    public List<Review> findAll(int count) {
        final String sql = """
                SELECT r.id_review              AS reviewId,
                       r.content                AS content,
                       r.is_positive            AS isPositive,
                       r.user_id                AS userId,
                       r.film_id                AS filmId,
                       COALESCE(r.useful, 0)    AS useful
                FROM reviews r
                ORDER BY useful DESC, reviewId ASC
                LIMIT ?
                """;
        return jdbc.query(sql, ROW_MAPPER, count);
    }

    @Override
    public void addLike(long reviewId, long userId) {
        jdbc.update("""
                MERGE INTO review_likes (review_id, user_id, is_positive)
                KEY (review_id, user_id) VALUES (?, ?, TRUE)
                """, reviewId, userId);
        recalcUseful(reviewId);
    }

    @Override
    public void addDislike(long reviewId, long userId) {
        jdbc.update("""
                MERGE INTO review_likes (review_id, user_id, is_positive)
                KEY (review_id, user_id) VALUES (?, ?, FALSE)
                """, reviewId, userId);
        recalcUseful(reviewId);
    }

    @Override
    public void removeLike(long reviewId, long userId) {
        jdbc.update("DELETE FROM review_likes WHERE review_id = ? AND user_id = ?", reviewId, userId);
        recalcUseful(reviewId);
    }

    @Override
    public void removeDislike(long reviewId, long userId) {
        jdbc.update("DELETE FROM review_likes WHERE review_id = ? AND user_id = ?", reviewId, userId);
        recalcUseful(reviewId);
    }

    private void recalcUseful(long reviewId) {
        final String sql = """
                UPDATE reviews r
                SET useful = (
                    SELECT COALESCE(SUM(CASE WHEN rl.is_positive THEN 1 ELSE -1 END), 0)
                    FROM review_likes rl
                    WHERE rl.review_id = r.id_review
                )
                WHERE r.id_review = ?
                """;
        jdbc.update(sql, reviewId);
    }
}
