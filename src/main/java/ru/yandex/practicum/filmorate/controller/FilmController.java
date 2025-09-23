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

    @GetMapping("/director/{directorId}")
    public List<FilmDto> getFilmsByDirector(@PathVariable Long directorId,
                                            @RequestParam(defaultValue = "year") String sortBy) {
        return filmService.getFilmsByDirector(directorId, sortBy);
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
    public void addLikes(@PathVariable @Positive(message = "id должен быть больше 0") Long id,
                         @PathVariable @Positive(message = "userId должен быть больше 0") Long userId) {
        filmService.addLikes(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void delLikes(@PathVariable @Positive(message = "id должен быть больше 0") Long id,
                         @PathVariable Long userId) {
        filmService.deleteLikes(id, userId);
    }

    @GetMapping("/popular")
    public List<FilmDto> popular(@RequestParam(defaultValue = "10") @Min(1) Integer count,
                                 @RequestParam(required = false)
                                 @Positive(message = "genreId должен быть больше 0") Long genreId,
                                 @RequestParam(required = false)
                                 @Min(value = 1, message = "year должен быть положительным") Integer year) {
        if (genreId == null && year == null) {
            return filmService.topFilms(count);
        }
        return filmService.getMostPopular(count, genreId, year);
    }

    @GetMapping("/common")
    public List<FilmDto> commonFilm(@RequestParam Long userId, @RequestParam Long friendId) {
        return filmService.getCommon(userId, friendId);
    }

    @DeleteMapping("/{filmId}")
    public void removeFilm(@PathVariable @Positive(message = "id должен быть больше 0") Long filmId) {
        filmService.deleteFilm(filmId);
    }

    @GetMapping("/search")
    public List<FilmDto> searchFilms(@RequestParam String query,
                                     @RequestParam String by) {
        log.info("Выполняется поиск фильмов с запросом '{}', параметр поиска: {}", query, by);
        return filmService.searchFilms(query, by);
    }
}