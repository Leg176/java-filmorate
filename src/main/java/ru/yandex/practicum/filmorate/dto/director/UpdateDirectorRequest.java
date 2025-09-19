package ru.yandex.practicum.filmorate.dto.director;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateDirectorRequest {

    public UpdateDirectorRequest() {}

    @Min(value = 1, message = "Id не может быть меньше 1.")
    private Long id;

    @NotBlank(message = "Имя не может быть пустым")
    private String name;

    public boolean hasFirstName() {
        return !name.isBlank();
    }
}
