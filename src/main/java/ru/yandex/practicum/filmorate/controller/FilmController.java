package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;

import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.serviceBD.FilmServiceBD;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmServiceBD filmServiceBD;

    @Autowired
    private FilmController(FilmServiceBD filmServiceBD) {
        this.filmServiceBD = filmServiceBD;
    }

    @GetMapping
    public Collection<FilmDto> findAll() {
        return filmServiceBD.getFilms();
    }

    @PostMapping
    public FilmDto create(@Valid @RequestBody NewFilmRequest filmRequest) {
        return filmServiceBD.createFilm(filmRequest);
    }

    @PutMapping
    public FilmDto update(@Valid @RequestBody UpdateFilmRequest filmRequest) {
        return filmServiceBD.updateFilm(filmRequest);
    }

    @GetMapping("/{id}")
    public FilmDto getFilm(@PathVariable Long id) {
        return filmServiceBD.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLikes(@PathVariable Long id, @PathVariable Long userId) {
        filmServiceBD.addLikes(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void delLikes(@PathVariable Long id, @PathVariable Long userId) {
        filmServiceBD.deleteLikes(id, userId);
    }

    @GetMapping("/popular")
    public List<FilmDto> topFilms(@RequestParam(defaultValue = "10") @Min(1) Integer count) {
        return filmServiceBD.topFilms(count);
    }
}
