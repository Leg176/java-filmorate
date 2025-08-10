package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;

import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;
    private final UserService userService;

    @Autowired
    private FilmController(FilmService filmService, UserService userService) {
        this.filmService = filmService;
        this.userService = userService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        return filmService.update(newFilm);
    }

    @GetMapping("/films/{id}")
    public Optional<Film> getFilm(@PathVariable Long id) {
        return filmService.getFilm(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLikes(@PathVariable Long id, @PathVariable Long userId) {
        if (filmService.getFilm(id).isEmpty()) {
            throw new NotFoundException("Фильм с id = " + id + " в коллекции не найден");
        }
        if (userService.getUser(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " в списках зарегестрированных не найден");
        }
        filmService.addLikes(filmService.getFilm(id).get(), userService.getUser(userId).get());
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void delLikes(@PathVariable Long id, @PathVariable Long userId) {
        if (filmService.getFilm(id).isEmpty()) {
            throw new NotFoundException("Фильм с id = " + id + " в коллекции не найден");
        }
        if (userService.getUser(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " в списках зарегестрированных не найден");
        }
        filmService.delLikes(filmService.getFilm(id).get(), userService.getUser(userId).get());
    }

    @GetMapping("/popular")
    public List<Film> topFilms(@RequestParam(defaultValue = "10") @Min(1) Integer count) {
        return filmService.topFilms(count);
    }
}
