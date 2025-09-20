package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.dto.review.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.review.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.model.Review;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository {
    Review create(NewReviewRequest req);
    Review update(UpdateReviewRequest req);
    void delete(long reviewId);
    Optional<Review> getById(long reviewId);

    List<Review> findByFilmId(long filmId, int count);
    List<Review> findAll(int count);

    void addLike(long reviewId, long userId);
    void addDislike(long reviewId, long userId);
    void removeLike(long reviewId, long userId);
    void removeDislike(long reviewId, long userId);
}
