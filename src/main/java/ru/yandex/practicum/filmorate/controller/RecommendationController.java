package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/users/{id}/recommendations")
public class RecommendationController {
    private final FilmService filmService;

    @Autowired
    public RecommendationController(FilmService filmService) {
        this.filmService = filmService;
    }

    /**
     * Возвращает рекомендации по фильмам для просмотра
     * @param id ID пользователя, для которого формируем рекомендации
     * @return Список рекомендуемых фильмов
     */
    @GetMapping
    public List<FilmDto> getRecommendations(@PathVariable("id") @Positive(message = "id должен быть больше 0") Long id) {
        return filmService.getRecommendations(id);
    }
}