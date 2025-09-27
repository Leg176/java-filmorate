package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.ReviewsRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dto.review.ReviewResponseDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.ReviewsMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.LikeType;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewsRepository reviewsRepository;
    private final EventService eventService;
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;

    public ReviewService(ReviewsRepository reviewsRepository, EventService eventService, UserRepository userRepository, FilmRepository filmRepository) {
        this.reviewsRepository = reviewsRepository;
        this.eventService = eventService;
        this.userRepository = userRepository;
        this.filmRepository = filmRepository;
    }

    public ReviewResponseDto create(Review review) {
        validationUser(review.getUserId());
        validationFilm(review.getFilmId());

        Review createdReview = reviewsRepository.save(review);
        eventService.add(createdReview.getId(), createdReview.getUserId(), EventType.REVIEW);
        return ReviewsMapper.toDto(createdReview);
    }

    public void delete(Long reviewId) {
        Review review = getById(reviewId);

        reviewsRepository.deleteReview(reviewId);
        eventService.delete(review.getId(), review.getUserId(), EventType.REVIEW);
    }

    public Review update(Review review) {
        Review currentReview = getById(review.getId());

        Review updatedReview = ReviewsMapper.updateReviewFields(currentReview, review);
        eventService.update(updatedReview.getId(), updatedReview.getUserId(), EventType.REVIEW);
        return reviewsRepository.update(updatedReview);
    }

    public void addLike(Long reviewId, Long userId, LikeType like) {
        getById(reviewId);
        validationUser(userId);

        reviewsRepository.insertLikeDislikeToReview(reviewId, userId, like);
    }

    public void deleteLike(Long reviewId, Long userId, LikeType like) {
        getById(reviewId);
        validationUser(userId);

        reviewsRepository.removeLikeFromReview(reviewId, userId, like);
    }

    public List<ReviewResponseDto> getByFilmId(Long id, Integer count) {
        return reviewsRepository.findByFilmId(id, count).stream()
                .map(ReviewsMapper::toDto)
                .toList();
    }

    public List<ReviewResponseDto> getAllWithCount(Integer count) {
        return reviewsRepository.findAll(count).stream()
                .map(ReviewsMapper::toDto)
                .toList();
    }

    public Review getById(Long reviewId) {
        return reviewsRepository.getById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id=" + reviewId + " не найден"));
    }

    private void validationUser(Long userId) {
        userRepository.getUser(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }

    private void validationFilm(Long filmId) {
        filmRepository.getFilm(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + filmId + " не найден"));
    }
}
