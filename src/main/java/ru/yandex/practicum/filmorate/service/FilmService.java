package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public void addLikes(Film film, User user) {
        film.getLikes().add(user.getId());
    }

    public void delLikes(Film film, User user) {
        film.getLikes().remove(user.getId());
    }

    public List<Film> topFilms(int quantity) {
        return filmStorage.findAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(quantity)
                .collect(Collectors.toList());
    }

    public Film create(Film film) {
        log.trace("Проверка даты релиза фильма на соблюдение требования ТЗ");
        if (!checkReleaseDate(film.getReleaseDate())) {
            log.warn("Дата выхода: {} фильма не должна быть ранее 25.12.1895 года", film.getDuration());
            throw new ValidationException("Дата выпуска фильма должна быть позже 25.12.1895г.");
        }
        return filmStorage.create(film);
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film update(Film newFilm) {
        log.trace("Обновление данных о фильме");
        if (newFilm.getId() == null) {
            log.warn("Не указан id фильма");
            throw new ValidationException("Id должен быть указан");
        }

        if (!checkReleaseDate(newFilm.getReleaseDate())) {
            log.warn("Дата выхода: {} фильма не должна быть ранее 25.12.1895 года", newFilm.getDuration());
            throw new ValidationException("Дата выпуска фильма должна быть позже 25.12.1895г.");
        }
        return filmStorage.update(newFilm);
    }

    public Optional<Film> getFilm(Long id) {
        return filmStorage.getFilm(id);
    }

    private boolean checkReleaseDate(LocalDate localDate) {
        log.debug("Проверяем дату выхода фильма");
        LocalDate firstFilmDate = LocalDate.of(1895, 12, 25);
        return localDate.isAfter(firstFilmDate);
    }
}
