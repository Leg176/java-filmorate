package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @Autowired
    private FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<FilmDto> findAll() {
        return filmService.getFilms();
    }

    @PostMapping
    public FilmDto create(@Valid @RequestBody NewFilmRequest newfilmRequest) {
        return filmService.createFilm(newfilmRequest);
    }

    @PutMapping
    public FilmDto update(@Valid @RequestBody UpdateFilmRequest updatefilmRequest) {
        return filmService.updateFilm(updatefilmRequest);
    }

    @GetMapping("/{id}")
    public FilmDto getFilm(@PathVariable @Positive(message = "id должен быть больше 0") Long id) {
        return filmService.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLikes(@PathVariable
                         @Positive(message = "id должен быть больше 0")
                         Long id,
                         @PathVariable
                         @Positive(message = "userId должен быть больше 0")
                         Long userId) {
        filmService.addLikes(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void delLikes(@PathVariable
                         @Positive(message = "id должен быть больше 0")
                         Long id,
                         @Positive(message = "userId должен быть больше 0")
                         @PathVariable Long userId) {
        filmService.deleteLikes(id, userId);
    }

    @GetMapping("/popular")
    public List<FilmDto> topFilms(@RequestParam(defaultValue = "10") @Min(1) Integer count) {
        return filmService.topFilms(count);
    }
}
