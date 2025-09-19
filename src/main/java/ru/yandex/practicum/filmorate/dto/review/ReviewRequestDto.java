package ru.yandex.practicum.filmorate.dto.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewRequestDto {
    Long reviewId;
    @NotNull
    @NotBlank
    String content;
    Boolean isPositive;
    Long userId;
    Long filmId;
    Integer useful;
}
