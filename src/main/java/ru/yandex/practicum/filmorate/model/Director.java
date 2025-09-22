package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NonNull;

@Data
public class Director {

    private Long id;

    @NotBlank(message = "Имя не может быть пустым")
    @NonNull
    private String name;

    public Director() {
    }
}
