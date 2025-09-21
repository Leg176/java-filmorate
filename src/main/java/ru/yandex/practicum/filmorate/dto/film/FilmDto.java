package ru.yandex.practicum.filmorate.dto.film;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NonNull;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilmDto {
    public FilmDto() {
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
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

    @NonNull
    @Min(value = 1, message = "Длительность фильма должна быть положительной")
    private Integer duration;

    private MotionPictureAssociation mpa;

    private Set<Genre> genres = new HashSet<>();

    private Long likes;
}
