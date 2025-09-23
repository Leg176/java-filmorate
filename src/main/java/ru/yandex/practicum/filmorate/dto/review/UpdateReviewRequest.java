package ru.yandex.practicum.filmorate.dto.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReviewRequest {

    @NotNull
    private Long idReview;     // нужен getIdReview()

    @NotBlank
    private String content;    // нужен getContent()

    @NotNull
    private Boolean isPositive; // нужен getIsPositive()
}
