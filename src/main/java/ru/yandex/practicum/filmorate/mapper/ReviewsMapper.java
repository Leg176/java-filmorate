package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.review.ReviewRequestDto;
import ru.yandex.practicum.filmorate.dto.review.ReviewResponseDto;
import ru.yandex.practicum.filmorate.model.Review;

public class ReviewsMapper {
    public static Review toReview(ReviewRequestDto dto) {
        return Review.builder()
                .id(dto.getReviewId())
                .content(dto.getContent())
                .isPositive(dto.getIsPositive())
                .userId(dto.getUserId())
                .filmId(dto.getFilmId())
                .useful(dto.getUseful())
                .build();
    }

    public static ReviewResponseDto toDto(Review review) {
        return ReviewResponseDto.builder()
                .reviewId(review.getId())
                .content(review.getContent())
                .isPositive(review.getIsPositive())
                .userId(review.getUserId())
                .filmId(review.getFilmId())
                .useful(review.getUseful())
                .build();
    }

    public static Review updateReviewFields(Review review, Review request) {
        review.setIsPositive(request.getIsPositive());
        if (request.getContent() != null) {
            review.setContent(request.getContent());
        }
        return review;
    }
}
