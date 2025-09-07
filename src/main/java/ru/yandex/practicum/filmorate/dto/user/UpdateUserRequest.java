package ru.yandex.practicum.filmorate.dto.user;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserRequest {
    /*@NotNull
    @Min(value = 1, message = "Id не может быть меньше 1.")*/
    private Long id;

    @Email(message = "Неверный формат электронной почты")
    private String email;

    @Pattern(regexp = "^\\S+$", message = "Логин не может содержать пробелы")
    private String login;

    private String name;

    @PastOrPresent
    private LocalDate birthday;

    public boolean hasLogin() {
        return !login.isBlank();
    }

    public boolean hasName() {
        return !name.isBlank();
    }

    public boolean hasEmail() {
        return !email.isBlank();
    }

    public boolean hasBirthday() {
        return birthday != null;
    }
}
