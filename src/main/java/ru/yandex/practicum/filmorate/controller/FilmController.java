package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получаем полный список фильмов содержащихся в коллекции");
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Добавляем новый фильм {} в коллекцию.", film);

        log.trace("Проверка даты релиза фильма на соблюдение требования ТЗ");
        if (!checkReleaseDate(film.getReleaseDate())) {
            log.warn("Дата выхода: {} фильма не должна быть ранее 25.12.1895 года", film.getDuration());
            throw new ValidationException("Дата выпуска фильма должна быть позже 25.12.1895г.");
        }
        log.trace("Присваиваем фильму уникальный id");
        film.setId(getNextId());
        // сохраняем новую публикацию в памяти приложения
        log.debug("Сохраняем фильм в коллекцию");
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлени с id: {}", film.getId());
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        log.trace("Обновление данных о фильме");
        if (newFilm.getId() == null) {
            log.warn("Не указан id фильма");
            throw new ValidationException("Id должен быть указан");
        }

        if (newFilm.getReleaseDate() != null  && !checkReleaseDate(newFilm.getReleaseDate())) {
            log.warn("Дата выхода: {} фильма не должна быть ранее 25.12.1895 года", newFilm.getDuration());
            throw new ValidationException("Дата выпуска фильма должна быть позже 25.12.1895г.");
        }
        log.info("Обновляем данные о фильме с id: {}.", newFilm.getId());
        log.trace("Проверка наличия в коллекции фильма с id указанным в теле метода PUT");
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());

            log.trace("Обновляем название фильма.");
            if (newFilm.getName() != null) {
                oldFilm.setName(newFilm.getName());
            }

            log.trace("Обновляем описание фильма.");
            if (newFilm.getDescription() != null) {
                oldFilm.setDescription(newFilm.getDescription());
            }

            log.trace("Обновляем дату выхода фильма.");
            if (newFilm.getReleaseDate() != null) {
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            }

            log.trace("Обновляем продолжительность фильма.");
            if (newFilm.getDuration() > 0) {
                oldFilm.setDuration(newFilm.getDuration());
            }

            log.info("Данные о фильме {} обновлены", oldFilm);
            return oldFilm;
        }

        log.warn("Фильм с id = {} не найден", newFilm.getId());
        throw new ValidationException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        log.debug("Генерируем id для фильма");
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private boolean checkReleaseDate(LocalDate localDate) {
        log.debug("Проверяем дату выхода фильма");
        LocalDate firstFilmDate = LocalDate.of(1895, 12, 25);
        return localDate.isAfter(firstFilmDate);
    }
}
