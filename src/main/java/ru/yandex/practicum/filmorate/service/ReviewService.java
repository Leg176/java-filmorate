package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.ReviewsRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dto.review.ReviewResponseDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
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
        if (review.getUserId() == null || review.getFilmId() == null) {
            throw new ValidationException("Неверный id");
        }
        userRepository.getUser(review.getUserId())
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + review.getUserId() + " не найден"));
        filmRepository.getFilm(review.getFilmId())
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + review.getFilmId() + " не найден"));

        Review createdReview = reviewsRepository.save(review);
        eventService.add(createdReview.getId(), createdReview.getUserId(), EventType.REVIEW);
        return ReviewsMapper.toDto(createdReview);
    }

    public void delete(Long id) {
        Review review = reviewsRepository.getById(id).orElseThrow(
                () -> new NotFoundException("пользователя с id :" + id + " не существует")
        );
        reviewsRepository.deleteReview(id);
        eventService.delete(review.getId(), review.getUserId(), EventType.REVIEW);
    }

    public Review update(Review review) {
        Review currentReview = reviewsRepository.getById(review.getId()).orElseThrow(
                () -> new NotFoundException("отзыв с id: " + review.getId() + " не найден")
        );
        Review updatedReview = ReviewsMapper.updateReviewFields(currentReview, review);
        eventService.update(updatedReview.getId(), updatedReview.getUserId(), EventType.REVIEW);
        return reviewsRepository.update(updatedReview);
    }

    public void addLike(Long reviewId, Long userId, LikeType like) {
        reviewsRepository.getById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id=" + reviewId + " не найден"));
        userRepository.getUser(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        reviewsRepository.insertLikeDislikeToReview(reviewId, userId, like);
    }

    public void deleteLike(Long reviewId, Long userId, LikeType like) {
        reviewsRepository.getById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id=" + reviewId + " не найден"));
        userRepository.getUser(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        reviewsRepository.removeLikeFromReview(reviewId, userId, like);
    }

    public List<ReviewResponseDto> getAllWithCount(Long id, Integer count) {
        return reviewsRepository.findAll(id, count).stream()
                .map(ReviewsMapper::toDto)
                .toList();
    }

    public List<ReviewResponseDto> getAllWithCount(Integer count) {
        return reviewsRepository.findAll(count).stream()
                .map(ReviewsMapper::toDto)
                .toList();
    }

    public Review getById(Long id) {
        return reviewsRepository.getById(id)
                .orElseThrow(() -> new NotFoundException("такого ревью не существует"));
    }
}
