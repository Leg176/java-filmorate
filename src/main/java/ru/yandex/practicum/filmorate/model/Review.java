package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Review {
    private Long id;
    @NotNull
    private String content;
    private Boolean isPositive;
    private Long userId;
    private Long filmId;
    private Integer useful;
}
