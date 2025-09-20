package ru.yandex.practicum.filmorate.dal.mappers;

import ru.yandex.practicum.filmorate.dto.review.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.review.ReviewDto;
import ru.yandex.practicum.filmorate.model.Review;

public final class ReviewMapper {
    private ReviewMapper() {}

    public static Review mapToReview(NewReviewRequest req) {
        Review r = new Review();
        r.setContent(req.getContent());
        r.setIsPositive(req.getIsPositive());
        r.setUserId(req.getUserId());
        r.setFilmId(req.getFilmId());
        r.setUseful(0);
        return r;
    }

    public static ReviewDto mapToDto(Review r) {
        ReviewDto dto = new ReviewDto();
        dto.setReviewId(r.getIdReview());
        dto.setContent(r.getContent());
        dto.setIsPositive(r.getIsPositive());
        dto.setUserId(r.getUserId());
        dto.setFilmId(r.getFilmId());
        dto.setUseful(r.getUseful() == null ? 0 : r.getUseful());
        return dto;
    }
}
