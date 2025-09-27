package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.review.ReviewRequestDto;
import ru.yandex.practicum.filmorate.dto.review.ReviewResponseDto;
import ru.yandex.practicum.filmorate.model.Review;

public class ReviewsMapper {

    /**
     * CREATE: игнорируем id и useful из DTO
     */
    public static Review toReviewForCreate(ReviewRequestDto dto) {
        return Review.builder()
                .id(null)
                .content(dto.getContent())
                .isPositive(dto.getIsPositive())
                .userId(dto.getUserId())
                .filmId(dto.getFilmId())
                .useful(null)
                .build();
    }

    /**
     * UPDATE: id берём из DTO, useful всё равно не маппим
     */
    public static Review toReviewForUpdate(ReviewRequestDto dto) {
        return Review.builder()
                .id(dto.getReviewId())
                .content(dto.getContent())
                .isPositive(dto.getIsPositive())
                .userId(dto.getUserId())
                .filmId(dto.getFilmId())
                .useful(null)
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

    public static Review updateReviewFields(Review current, Review request) {
        current.setIsPositive(request.getIsPositive());
        if (request.getContent() != null) {
            current.setContent(request.getContent());
        }
        return current;
    }
}