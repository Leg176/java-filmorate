package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.review.ReviewRequestDto;
import ru.yandex.practicum.filmorate.dto.review.ReviewResponseDto;
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
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reviewService.create(ReviewsMapper.toReview(dto)));
    }

    @PutMapping
    public ResponseEntity<ReviewResponseDto> updateReview(@RequestBody ReviewRequestDto dto) {
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Контент отзыва не может быть пустым");
        }

        Review review = reviewService.update(ReviewsMapper.toReview(dto));
        return ResponseEntity.status(HttpStatus.OK).body(ReviewsMapper.toDto(review));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable("id") Long id) {
        reviewService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponseDto> getReviewsById(@PathVariable("id") Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ReviewsMapper.toDto(reviewService.getById(id)));
    }


    @GetMapping
    public ResponseEntity<List<ReviewResponseDto>> getReviewsWithCountAndId(
            @RequestParam(value = "filmId", required = false) Optional<Long> filmId,
            @RequestParam(value = "count", defaultValue = "10") Integer count) {

        if (filmId.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(reviewService.getByFilmId(filmId.get(), count));
        } else {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(reviewService.getAllWithCount(count));
        }
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(@PathVariable("id") Long reviewId,
                                        @PathVariable("userId") Long userId) {
        reviewService.addLike(reviewId, userId, LikeType.LIKE);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Void> addDislike(@PathVariable("id") Long reviewId,
                                           @PathVariable("userId") Long userId) {
        reviewService.addLike(reviewId, userId, LikeType.DISLIKE);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> deleteLike(@PathVariable("id") Long reviewId,
                                           @PathVariable("userId") Long userId) {
        reviewService.deleteLike(reviewId, userId, LikeType.LIKE);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Void> deleteDislike(@PathVariable("id") Long reviewId,
                                              @PathVariable("userId") Long userId) {
        reviewService.deleteLike(reviewId, userId, LikeType.DISLIKE);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
