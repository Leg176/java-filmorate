package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RecommendationService {
    private final FilmRepository filmRepository;

    public RecommendationService(FilmRepository filmRepository) {
        this.filmRepository = filmRepository;
    }


    public List<FilmDto> getRecommendations(Long userId) {
        return filmRepository.getRecommendations(userId).stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }
}
