package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {

    // Вывод всех фильмов содержащихся в коллекции
    Collection<Film> findAll();

    // Добавление нового фильма в коллекцию
    Film create(Film film);

    // Обновления данных о фильме в коллекции
    Film update(Film newFilm);

    // Вывод фильма по его id
    Optional<Film> getFilm(Long id);
}
