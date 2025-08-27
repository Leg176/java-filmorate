package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
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
    private final UserService userService;

    private static final LocalDate FIRST_FILM_DATE = LocalDate.of(1895, 12, 25);

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public void addLikes(Long id, Long userId) {
        if (getFilm(id).isEmpty()) {
            throw new NotFoundException("Фильм с id = " + id + " в коллекции не найден");
        }
        if (userService.getUser(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " в списках зарегестрированных не найден");
        }
        getFilm(id).get().getLikes().add(userId);
    }

    public void delLikes(Long id, Long userId) {
        if (getFilm(id).isEmpty()) {
            throw new NotFoundException("Фильм с id = " + id + " в коллекции не найден");
        }
        if (userService.getUser(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " в списках зарегестрированных не найден");
        }
        getFilm(id).get().getLikes().remove(userId);
    }

    public List<Film> topFilms(int quantity) {
        return filmStorage.findAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(quantity)
                .collect(Collectors.toList());
    }

    public Film create(Film film) {
        log.info("Добавляем новый фильм {} в коллекцию.", film);
        log.trace("Присваиваем фильму уникальный id");
        film.setIdFilm(getNextId());
        log.trace("Проверка даты релиза фильма на соблюдение требования ТЗ");
        checkReleaseDate(film.getReleaseDate());
        return filmStorage.create(film);
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film update(Film newFilm) {
        log.trace("Обновление данных о фильме");
        if (newFilm.getIdFilm() == null) {
            log.warn("Не указан id фильма");
            throw new ValidationException("Id должен быть указан");
        }
        checkReleaseDate(newFilm.getReleaseDate());
        return filmStorage.update(newFilm);
    }

    public Optional<Film> getFilm(Long id) {
        if (filmStorage.findAll() == null) {
            return Optional.empty();
        }
        return filmStorage.getFilm(id);
    }

    private void checkReleaseDate(LocalDate localDate) {
        log.debug("Проверяем дату выхода фильма");
        if (!localDate.isAfter(FIRST_FILM_DATE)) {
            log.warn("Дата выхода: {} фильма не должна быть ранее 25.12.1895 года", localDate);
            throw new ValidationException("Дата выпуска фильма должна быть позже 25.12.1895г.");
        }
    }

    private long getNextId() {
        log.debug("Генерируем id для фильма");
        long currentMaxId = filmStorage.findAll()
                .stream()
                .map(Film::getIdFilm)
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
