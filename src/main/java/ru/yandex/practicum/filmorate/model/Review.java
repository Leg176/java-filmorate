package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private Long id;
    @NotNull
    private String content;
    private Long idReview;
    private Boolean isPositive;
    private Long userId;
    private Long filmId;
    private Integer useful;
}
