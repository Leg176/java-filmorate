package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(of = {"id"})
public class User {
    private Long id;
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
    private Set<Long> friends = new HashSet<>();
}
