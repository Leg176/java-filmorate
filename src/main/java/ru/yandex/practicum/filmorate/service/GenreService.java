package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class GenreService {
    private final GenreRepository genreRepository;

    @Autowired
    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public Genre getGenre(Long idGenre) {
        Optional<Genre> genre = genreRepository.getGenre(idGenre);
        if (genre.isEmpty()) {
            throw new NotFoundException("Жанр не обнаружен");
        }
        return genre.get();
    }

    public List<Genre> getAllGenre() {
        return genreRepository.findAll();
    }

    public List<Genre> getGenresByIdFilm(Long idFilm) {
        return genreRepository.findGenresFilm(idFilm);
    }
}
