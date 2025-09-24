package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.ReviewsRowMapper;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.enums.LikeType;

import java.util.List;
import java.util.Optional;


@Slf4j
@Repository
public class ReviewsRepository extends BaseRepository<Review> {
    private static final String FIND_ALL_QUERY = """
            SELECT r.id, r.content, r.isPositive, r.idUser, r.idFilm,
                   COALESCE(SUM(CASE
                       WHEN rl.likeType = 'LIKE' THEN 1
                       WHEN rl.likeType = 'DISLIKE' THEN -1
                       ELSE 0 END), 0) AS useful
            FROM Reviews AS r
            LEFT OUTER JOIN REVIEW_LIKES rl ON rl.idReview = r.id
            WHERE r.idFilm = ?
            GROUP BY r.ID
            ORDER BY useful DESC, r.id ASC
            LIMIT ?
            """;
    private static final String FIND_ALL_QUERY_WITH_COUNT = """
            SELECT r.id, r.content, r.isPositive, r.idUser, r.idFilm, sum(CASE
                                                                            WHEN rl.likeType = 'LIKE' THEN 1
                                                                            WHEN rl.likeType = 'DISLIKE' THEN -1
                                                                            ELSE 0 END) AS useful
            FROM Reviews AS r
            LEFT OUTER JOIN REVIEW_LIKES rl ON rl.idReview = r.id
            GROUP BY r.ID
            ORDER BY r.id desc
            LIMIT ?
            """;
    private static final String FIND_BY_ID_QUERY = """
            SELECT r.id, r.content, r.isPositive, r.idUser, r.idFilm, sum(CASE
                                                                            WHEN rl.likeType = 'LIKE' THEN 1
                                                                            WHEN rl.likeType = 'DISLIKE' THEN -1
                                                                            ELSE 0 END) AS useful
            FROM Reviews AS r
            LEFT OUTER JOIN REVIEW_LIKES rl ON rl.idReview = r.id
            WHERE r.id = ?
            GROUP BY r.ID
            """;
    private static final String INSERT_QUERY = "INSERT INTO Reviews(content, isPositive, idUser, idFilm) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE Reviews SET content = ?, isPositive = ? WHERE id = ?";
    private static final String INSERT_LIKE_DISLIKE_QUERY = "MERGE INTO REVIEW_LIKES (idReview, idUser, likeType) KEY (idReview, idUser) VALUES(?, ?, ?)";
    private static final String REMOVE_REVIEW_QUERY = "DELETE FROM Reviews " +
            "WHERE id = ?";
    private static final String REMOVE_LIKE_FROM_REVIEW_QUERY = "DELETE FROM REVIEW_LIKES " +
            "WHERE idReview = ? AND idUser = ? AND likeType = ?";

    public ReviewsRepository(JdbcTemplate jdbc, ReviewsRowMapper mapper) {
        super(jdbc, mapper);
    }

    public Review save(Review review) {
        long id = insert(INSERT_QUERY, "id",
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId()
        );
        review.setId(id);
        return review;
    }

    public List<Review> findAll(Long filmId, Integer count) {
        return findMany(FIND_ALL_QUERY, filmId, count);
    }

    public List<Review> findAll(Integer count) {
        return findMany(FIND_ALL_QUERY_WITH_COUNT, count);
    }

    public Optional<Review> getById(long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public Review update(Review review) {
        update(
                UPDATE_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getId()
        );
        return review;
    }

    public void deleteReview(Long reviewId) {
        if (!delete(REMOVE_REVIEW_QUERY, reviewId)) {
            throw new InternalServerException("Не найден отзыв для удаления");
        }
    }

    public void insertLikeDislikeToReview(Long reviewId, Long userId, LikeType likeType) {
        update(
                INSERT_LIKE_DISLIKE_QUERY,
                reviewId,
                userId,
                likeType.name()
        );
    }

    public void removeLikeFromReview(Long reviewId, Long userId, LikeType likeType) {
        if (!delete(REMOVE_LIKE_FROM_REVIEW_QUERY, reviewId, userId, likeType.name())) {
            throw new InternalServerException("Не найден лайк/дизлайк для удаления");
        }
    }


}
