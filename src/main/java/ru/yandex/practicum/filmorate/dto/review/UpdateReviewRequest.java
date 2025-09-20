package ru.yandex.practicum.filmorate.dto.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpdateReviewRequest {

    @NotNull
    @Positive
    private Long reviewId;

    @NotBlank
    private String content;

    @NotNull
    private Boolean isPositive;

    public Long getIdReview() {
        return reviewId;
    }
}
