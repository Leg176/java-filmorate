package ru.yandex.practicum.filmorate.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class NewUserRequest {

    @Email(message = "Неверный формат электронной почты")
    @NonNull
    private String email;

    @NonNull
    @NotBlank(message = "Логин должен быть указан")
    @Pattern(regexp = "^\\S+$", message = "Логин не может содержать пробелы")
    private String login;

    private String name;

    @NonNull
    @PastOrPresent
    private LocalDate birthday;

    Set<Long> friendship = new HashSet<>();

    public void checkName() {
        if(name.isBlank()) {
            this.name = login;
        }
    }
}
