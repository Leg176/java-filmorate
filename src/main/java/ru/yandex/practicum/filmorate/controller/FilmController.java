package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;

import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmStorage filmStorage;
    private final FilmService filmService;
    private final UserStorage userStorage;

    @Autowired
    private FilmController(FilmStorage filmStorage, FilmService filmService, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
        this.userStorage = userStorage;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        return filmStorage.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        return filmStorage.update(newFilm);
    }

    @GetMapping("/films/{id}")
    public Optional<Film> getFilm(@PathVariable Long id) {
        return filmStorage.getFilm(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLikes(@PathVariable Long id, @PathVariable Long userId) {
        if (filmStorage.getFilm(id).isEmpty()) {
            throw new NotFoundException("Фильм с id = " + id + " в коллекции не найден");
        }
        if (userStorage.getUser(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " в списках зарегестрированных не найден");
        }
        filmService.addLikes(filmStorage.getFilm(id).get(), userStorage.getUser(userId).get());
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void delLikes(@PathVariable Long id, @PathVariable Long userId) {
        if (filmStorage.getFilm(id).isEmpty()) {
            throw new NotFoundException("Фильм с id = " + id + " в коллекции не найден");
        }
        if (userStorage.getUser(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " в списках зарегестрированных не найден");
        }
        filmService.delLikes(filmStorage.getFilm(id).get(), userStorage.getUser(userId).get());
    }

    @GetMapping("/popular")
    public List<Film> topFilms(@RequestParam(defaultValue = "10") @Min(1) Integer count) {
        return filmService.topFilms(count);
    }
}
