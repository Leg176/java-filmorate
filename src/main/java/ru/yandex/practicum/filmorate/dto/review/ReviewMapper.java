package ru.yandex.practicum.filmorate.dto.review;

import ru.yandex.practicum.filmorate.model.Review;

public final class ReviewMapper {
    private ReviewMapper() {
    }

    public static ReviewDto mapToDto(Review r) {
        return new ReviewDto(
                r.getIdReview(),
                r.getContent(),
                r.getIsPositive(),
                r.getUserId(),
                r.getFilmId(),
                r.getUseful()
        );
    }
}
