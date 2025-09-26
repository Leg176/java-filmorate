package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.dal.MpaRepository;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RecommendationService {
    private final FilmRepository filmRepository;
    private final MpaRepository mpaRepository;
    private final GenreRepository genreRepository;
    private final DirectorRepository directorRepository;

    public RecommendationService(FilmRepository filmRepository, MpaRepository mpaRepository,
                                 GenreRepository genreRepository, DirectorRepository directorRepository) {
        this.filmRepository = filmRepository;
        this.mpaRepository = mpaRepository;
        this.genreRepository = genreRepository;
        this.directorRepository = directorRepository;
    }

    public List<FilmDto> getRecommendations(Long userId) {
        List<Film> films = filmRepository.getRecommendations(userId);
        List<Long> ids = films.stream().map(Film::getIdFilm).distinct().toList();
        Map<Long, MotionPictureAssociation> mpaMap = mpaRepository.findMpaByFilmIds(ids);
        Map<Long, Set<Genre>> genreMap = genreRepository.findGenresByFilmIds(ids);
        Map<Long, Set<Director>> directorMap = directorRepository.findDirectorByFilmIds(ids);
        for (Film f : films) {
            f.setMpa(mpaMap.get(f.getIdFilm()));
            f.setGenres(genreMap.getOrDefault(f.getIdFilm(), new HashSet<>()));
            f.setDirector(directorMap.getOrDefault(f.getIdFilm(), new HashSet<>()));
        }
        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }
}
