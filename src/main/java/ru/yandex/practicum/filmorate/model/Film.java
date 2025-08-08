package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Film.
 */

@Data
@EqualsAndHashCode(of = {"id"})
public class Film {
    private Long id;
    @NotBlank(message = "Название не может быть пустым")
    @NonNull
    private String name;
    @NonNull
    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description;
    @NonNull
    @PastOrPresent
    private LocalDate releaseDate;
    @Min(value = 1, message = "Длительность фильма должна быть положительной")
    private int duration;
    private Set<Long> likes = new HashSet<>();
}
