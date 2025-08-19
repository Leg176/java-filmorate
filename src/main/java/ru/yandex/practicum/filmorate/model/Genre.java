package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NonNull;

@Data
public class Genre {
    private Long idGenre;
    @NotBlank(message = "Название не может быть пустым")
    @NonNull
    private String name;
}
