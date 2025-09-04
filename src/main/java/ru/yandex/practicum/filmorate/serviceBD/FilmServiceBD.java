package ru.yandex.practicum.filmorate.serviceBD;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmServiceBD {

    private final FilmRepository filmRepository;
    private final GenreRepository genreRepository;
    private final UserRepository userRepository;

    @Autowired
    public FilmServiceBD(FilmRepository filmRepository, GenreRepository genreRepository, UserRepository userRepository) {
        this.filmRepository = filmRepository;
        this.genreRepository = genreRepository;
        this.userRepository = userRepository;
    }

    public FilmDto createFilm(NewFilmRequest request) {
        if (request == null) {
            throw new ValidationException("Запрос на добавление нового фильма не может быть пустым");
        }
        Optional<Film> alreadyExistFilm = filmRepository.findByNameFilm(request.getNameFilm());
        if (alreadyExistFilm.isPresent()) {
            throw new ValidationException("Фильм с названием " + request.getNameFilm() + " уже существует");
        }
        Film film = FilmMapper.mapToFilm(request);
        Film filmWithId = filmRepository.save(film);
        return FilmMapper.mapToFilmDto(filmWithId);
    }

    public FilmDto getFilmById(long idFilm) {
        if (idFilm <= 0) {
            throw new ValidationException("id не может быть отрицательными или равными 0");
        }
        Film film = filmRepository.getFilm(idFilm)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + idFilm + " не найден"));
        List<Long> likes = filmRepository.findAllLikes(idFilm);
        List<Genre> genres = genreRepository.findGenresFilm(idFilm);
        film.setLikes(new HashSet<>(likes));
        film.setGenres(new HashSet<>(genres));
        return FilmMapper.mapToFilmDto(film);
    }

    public List<FilmDto> getFilms() {
        return filmRepository.findAll()
                .stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public FilmDto updateFilm(UpdateFilmRequest request) {
        if (request == null) {
            throw new ValidationException("Запрос на обновление данных фильма не может быть пустым");
        }
        Film updatedFilm = filmRepository.getFilm(request.getIdFilm())
                .map(film -> FilmMapper.updateFilmFields(film, request))
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + request.getIdFilm() + " не найден"));
        filmRepository.update(updatedFilm);
        return FilmMapper.mapToFilmDto(updatedFilm);
    }

    public List<FilmDto> topFilms(int quantity) {
        return filmRepository.findTopFilm(quantity)
                .stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public void addLikes(Long idFilm, Long idUser) {
        validationInLikes(idFilm, idUser);
        filmRepository.addLike(idFilm, idUser);
    }

    public void deleteLikes(Long idFilm, Long idUser) {
        validationInLikes(idFilm, idUser);
        filmRepository.deleteLike(idFilm, idUser);
    }

    private void validationInLikes(Long idFilm, Long idUser) {
        if (idFilm <= 0 || idUser <= 0) {
            throw new ValidationException("id не могут быть отрицательными или равными 0");
        }
        Optional<Film> film = filmRepository.getFilm(idFilm);
        Optional<User> user = userRepository.getUser(idUser);
        if (film.isEmpty() || user.isEmpty()) {
            throw new ValidationException("Фильм/пользователя в базе данных не существует");
        }
    }
}
