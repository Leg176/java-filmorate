package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.ReviewsRepository;
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

    public ReviewService(ReviewsRepository reviewsRepository, EventService eventService) {
        this.reviewsRepository = reviewsRepository;
        this.eventService = eventService;
    }

    public ReviewResponseDto create(Review review) {
        if (review.getUserId() <= 0 || review.getFilmId() <= 0) {
            throw new NotFoundException("Неверный id");
        }
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
                () -> new NotFoundException("отзыв с id: " + review.getId() + " не найдет")
        );
        Review updatedReview = ReviewsMapper.updateReviewFields(currentReview, review);
        eventService.update(updatedReview.getId(), updatedReview.getUserId(), EventType.REVIEW);
        return reviewsRepository.update(updatedReview);
    }

    public void addLike(Long reviewId, Long userId, LikeType like) {
        reviewsRepository.insertLikeDislikeToReview(reviewId, userId, like);
    }

    public void deleteLike(Long reviewId, Long userId, LikeType like) {
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
