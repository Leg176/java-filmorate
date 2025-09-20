package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Review {
    private Long idReview;
    private String content;
    private Boolean isPositive;
    private Long userId;
    private Long filmId;
    private Integer useful;
}
