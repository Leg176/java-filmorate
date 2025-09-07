package ru.yandex.practicum.filmorate.dto.film;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UpdateFilmRequest {
    @NotNull
    @Min(value = 1, message = "Id не может быть меньше 1.")
    private Long id;

    private String nameFilm;

    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description;

    @PastOrPresent
    private LocalDate releaseDate;

    private Integer duration;

    private MotionPictureAssociation mpa;

    private final Set<Genre> genres;

    public boolean hasNameFilm() {
        return !nameFilm.isBlank();
    }

    public boolean hasDescription() {
        return !description.isBlank();
    }

    public boolean hasReleaseDate() {
        return releaseDate != null;
    }

    public boolean hasDuration() {
        return duration != null && duration >= 1;
    }

    public boolean hasMpa() {
        return mpa != null;
    }

    public boolean hasGenres() {
        return genres != null;
    }
}
