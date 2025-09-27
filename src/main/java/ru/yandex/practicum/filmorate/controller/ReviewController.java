package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.review.ReviewRequestDto;
import ru.yandex.practicum.filmorate.dto.review.ReviewResponseDto;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.ReviewsMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.enums.LikeType;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponseDto> createReview(@Valid @RequestBody ReviewRequestDto dto) {
        ReviewResponseDto created = reviewService.create(ReviewsMapper.toReviewForCreate(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping
    public ResponseEntity<ReviewResponseDto> updateReview(@Valid @RequestBody ReviewRequestDto dto) {
        if (dto.getReviewId() == null) {
            throw new ValidationException("reviewId обязателен для обновления");
        }
        Review updated = reviewService.update(ReviewsMapper.toReviewForUpdate(dto));
        return ResponseEntity.ok(ReviewsMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable("id") Long id) {
        reviewService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponseDto> getReviewsById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ReviewsMapper.toDto(reviewService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponseDto>> getReviewsWithCountAndId(
            @RequestParam(value = "filmId", required = false) Optional<Long> filmId,
            @RequestParam(value = "count", defaultValue = "10") Integer count) {
        List<ReviewResponseDto> body = filmId.isPresent()
                ? reviewService.getAllWithCount(filmId.get(), count)
                : reviewService.getAllWithCount(count);
        return ResponseEntity.ok(body);
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(@PathVariable("id") Long reviewId,
                                        @PathVariable("userId") Long userId) {
        reviewService.addLike(reviewId, userId, LikeType.LIKE);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Void> addDislike(@PathVariable("id") Long reviewId,
                                           @PathVariable("userId") Long userId) {
        reviewService.addLike(reviewId, userId, LikeType.DISLIKE);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> deleteLike(@PathVariable("id") Long reviewId,
                                           @PathVariable("userId") Long userId) {
        reviewService.deleteLike(reviewId, userId, LikeType.LIKE);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Void> deleteDislike(@PathVariable("id") Long reviewId,
                                              @PathVariable("userId") Long userId) {
        reviewService.deleteLike(reviewId, userId, LikeType.DISLIKE);
        return ResponseEntity.ok().build();
    }
}