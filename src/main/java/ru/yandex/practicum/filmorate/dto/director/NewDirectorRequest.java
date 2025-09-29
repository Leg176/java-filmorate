package ru.yandex.practicum.filmorate.dto.director;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class NewDirectorRequest {

    private Long id;

    @NotBlank(message = "Имя не может быть пустым")
    @NonNull
    private String name;
}
