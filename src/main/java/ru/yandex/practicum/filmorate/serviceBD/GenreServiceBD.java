package ru.yandex.practicum.filmorate.serviceBD;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Slf4j
@Service
public class GenreServiceBD {
    private final GenreRepository genreRepository;

    @Autowired
    public GenreServiceBD(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public Genre getGenre(long idGenre) {

        if (idGenre <= 0) {
            throw new ValidationException("id не может быть отрицательными или равными 0");
        }
        if (genreRepository.getGenre(idGenre).isEmpty()) {
            throw new NotFoundException("Жанр не обнаружен");
        }
        return genreRepository.getGenre(idGenre).get();
    }

    public List<Genre> getAllGenre() {
        return genreRepository.findAll();
    }

    public List<Genre> getGenresByIdFilm(long idFilm) {
        if (idFilm <= 0) {
            throw new ValidationException("id не может быть отрицательными или равными 0");
        }
        return genreRepository.findGenresFilm(idFilm);
    }
}
