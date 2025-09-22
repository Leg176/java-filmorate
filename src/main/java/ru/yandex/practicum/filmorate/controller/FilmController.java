package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public List<FilmDto> findAll() {
        return filmService.getFilms();
    }

    @PostMapping
    public FilmDto create(@Valid @RequestBody NewFilmRequest newFilmRequest) {
        return filmService.createFilm(newFilmRequest);
    }

    @PutMapping
    public FilmDto update(@Valid @RequestBody UpdateFilmRequest updateFilmRequest) {
        return filmService.updateFilm(updateFilmRequest);
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
                         @PathVariable @Positive(message = "userId должен быть больше 0") Long userId) {
        filmService.deleteLikes(id, userId);
    }

    @GetMapping("/popular")
    public List<FilmDto> getMostPopular(
            @RequestParam(value = "count", required = false, defaultValue = "10") @Min(1) Integer count,
            @RequestParam(value = "genreId", required = false) @Positive Long genreId,
            @RequestParam(value = "year", required = false) Integer year
    ) {
        return filmService.getMostPopular(count, genreId, year);
    }
}