package ru.yandex.practicum.filmorate.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.ReviewRepository;
import ru.yandex.practicum.filmorate.dal.mappers.ReviewMapper;
import ru.yandex.practicum.filmorate.dto.review.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.review.ReviewDto;
import ru.yandex.practicum.filmorate.dto.review.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.Operation;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviews;
    private final UserService userService;
    private final FilmService filmService;
    private final FeedService feedService;

    @Transactional
    public ReviewDto create(NewReviewRequest req) {
        userService.getUserById(req.getUserId());
        filmService.getFilmById(req.getFilmId());

        Review created = reviews.create(req);

        feedService.recordEvent(created.getUserId(), EventType.REVIEW, Operation.ADD, created.getIdReview());

        return ReviewMapper.mapToDto(created);
    }

    @Transactional
    public ReviewDto update(UpdateReviewRequest req) {
        reviews.getById(req.getIdReview())
                .orElseThrow(() -> new NotFoundException("Отзыв не найден: id=" + req.getIdReview()));

        Review updated = reviews.update(req);

        feedService.recordEvent(updated.getUserId(), EventType.REVIEW, Operation.UPDATE, updated.getIdReview());

        return ReviewMapper.mapToDto(updated);
    }

    @Transactional
    public void delete(long reviewId) {
        Review before = reviews.getById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв не найден: id=" + reviewId));

        reviews.delete(reviewId);

        feedService.recordEvent(before.getUserId(), EventType.REVIEW, Operation.REMOVE, reviewId);
    }

    public ReviewDto getById(Long id) {
        Review r = reviews.getById(id).orElseThrow(() -> new NotFoundException("Отзыв не найден: id=" + id));
        return ReviewMapper.mapToDto(r);
    }

    public List<ReviewDto> getReviews(Long filmId, Integer count) {
        int limit = (count == null || count <= 0) ? 10 : count;
        List<Review> list = (filmId == null)
                ? reviews.findAll(limit)
                : reviews.findByFilmId(filmId, limit);
        return list.stream().map(ReviewMapper::mapToDto).toList();
    }

    @Transactional
    public ReviewDto addLike(long reviewId, long userId) {
        userService.getUserById(userId);
        reviews.addLike(reviewId, userId);
        return getById(reviewId);
    }

    @Transactional
    public ReviewDto addDislike(long reviewId, long userId) {
        userService.getUserById(userId);
        reviews.addDislike(reviewId, userId);
        return getById(reviewId);
    }

    @Transactional
    public ReviewDto removeLike(long reviewId, long userId) {
        reviews.removeLike(reviewId, userId);
        return getById(reviewId);
    }

    @Transactional
    public ReviewDto removeDislike(long reviewId, long userId) {
        reviews.removeDislike(reviewId, userId);
        return getById(reviewId);
    }
}
