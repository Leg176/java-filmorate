package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.dto.director.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.director.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.model.Director;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DirectorMapper {

    public static Director mapToDirector(NewDirectorRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request не может быть пустым!");
        }
        Director director = new Director();
        director.setName(request.getName());
        return director;
    }

    public static Director updateDirectorFields(Director director, UpdateDirectorRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request не может быть пустым!");
        }

        if (request.hasFirstName()) {
            director.setName(request.getName());
        }
        return director;
    }
}
