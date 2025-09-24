package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.dal.MpaRepository;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RecommendationService {
    private final FilmRepository filmRepository;
    private final MpaRepository mpaRepository;
    private final GenreRepository genreRepository;

    public RecommendationService(FilmRepository filmRepository, MpaRepository mpaRepository,
                                 GenreRepository genreRepository) {
        this.filmRepository = filmRepository;
        this.mpaRepository = mpaRepository;
        this.genreRepository = genreRepository;
    }


    public List<FilmDto> getRecommendations(Long userId) {
        return filmRepository.getRecommendations(userId).stream()
                .map(id -> {
                    List<Genre> genre = genreRepository.findGenresFilm(id.getIdFilm());
                    Optional<MotionPictureAssociation> mpaOpt = mpaRepository.getMpa(id.getMpa().getId());
                    if (mpaOpt.isEmpty()) {
                        throw new NotFoundException("Mpa в базе данных не найдено");
                    }
                    MotionPictureAssociation mpa = mpaOpt.get();
                    id.setMpa(mpa);
                    id.setGenres(new HashSet<>(genre));
                    return id;
                })
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }
}
