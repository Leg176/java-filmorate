package ru.yandex.practicum.filmorate.serviceBD;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmServiceBD {

    private final FilmRepository filmRepository;
    private final GenreServiceBD genreServiceBD;
    private final MpaServiceBD mpaServiceBD;
    private final UserServiceBD userServiceBD;

    private static final LocalDate FIRST_FILM_DATE = LocalDate.of(1895, 12, 25);

    @Autowired
    public FilmServiceBD(FilmRepository filmRepository, MpaServiceBD mpaServiceBD, GenreServiceBD genreServiceBD,
                         UserServiceBD userServiceBD) {
        this.filmRepository = filmRepository;
        this.mpaServiceBD = mpaServiceBD;
        this.genreServiceBD = genreServiceBD;
        this.userServiceBD = userServiceBD;
    }

    public FilmDto createFilm(NewFilmRequest request) {
        try {
            validationRequest(request);
            checkReleaseDate(request.getReleaseDate());
            MotionPictureAssociation mpaBD = mpaServiceBD.getMpa(request.getMpa().getId());
            Set<Genre> genres = request.getGenres();
            Set<Genre> validGenres = validationGenres(genres);

            Film film = FilmMapper.mapToFilm(request);
            film.setMpa(mpaBD);
            film.setGenres(new HashSet<>(validGenres));

            Film savedFilm = filmRepository.save(film);
            log.info("Фильм сохранен с ID: {}", savedFilm.getIdFilm());
            savedFilm.getGenres().stream()
                    .map(Genre::getId)
                    .forEach(id -> addGenres(savedFilm.getIdFilm(), id));
            addMpaBD(savedFilm.getIdFilm(), mpaBD.getId());
            return FilmMapper.mapToFilmDto(savedFilm);
        } catch (Exception e) {
            log.error("Ошибка при создании фильма", e);
            throw e;
        }
    }

    public FilmDto getFilmById(Long idFilm) {
        validationId(idFilm);
        Optional<Film> filmOpt = filmRepository.getFilm(idFilm);
        if (filmOpt.isEmpty()) {
            throw new NotFoundException("Фильм с id = " + idFilm + " в базе данных не найден");
        }
        Film film = filmOpt.get();
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
        if (request.hasReleaseDate()) {
            checkReleaseDate(request.getReleaseDate());
        }
        validationId(request.getId());

        Film film = validationFilm(request.getId());
        Film updatedFilm = FilmMapper.updateFilmFields(film, request);

        mpaServiceBD.isExistsMpa(updatedFilm.getMpa().getId());
        Set<Genre> validGenreUpdate = validationGenres(updatedFilm.getGenres());
        updatedFilm.setGenres(validGenreUpdate);
        deleteMpaBD(film, film.getMpa());
        deleteGenreBD(film);

        filmRepository.update(updatedFilm);
        updatedFilm.getGenres().stream()
                .map(Genre::getId)
                .forEach(id -> addGenres(updatedFilm.getIdFilm(), id));
        addMpaBD(updatedFilm.getIdFilm(), updatedFilm.getMpa().getId());
        return FilmMapper.mapToFilmDto(updatedFilm);
    }

    public List<FilmDto> topFilms(int count) {
        return filmRepository.findTopFilm(count).stream()
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

    private void addMpaBD(Long idFilm, Long idMpa) {
        filmRepository.addMpa(idFilm, idMpa);
    }

    private void addGenres(Long idFilm, Long idGenre) {
        filmRepository.addGenre(idFilm, idGenre);
    }

    private void deleteMpaBD(Film film, MotionPictureAssociation mpa) {
        filmRepository.delMpa(film.getIdFilm(), mpa.getId());
    }

    private void deleteGenreBD(Film film) {
        film.getGenres().stream()
                .map(Genre::getId)
                .forEach(id -> filmRepository.delGenre(film.getIdFilm(), id));
    }

    private void checkReleaseDate(LocalDate localDate) {
        log.debug("Проверяем дату выхода фильма");
        if (!localDate.isAfter(FIRST_FILM_DATE)) {
            log.warn("Дата выхода: {} фильма не должна быть ранее 25.12.1895 года", localDate);
            throw new ValidationException("Дата выпуска фильма должна быть позже 25.12.1895г.");
        }
    }

    private void validationInLikes(Long idFilm, Long idUser) {
        validationId(idFilm);
        validationId(idUser);
        userServiceBD.validationUserIsEmpty(idUser);
        validationFilm(idFilm);
    }

    private void validationRequest(NewFilmRequest request) {
        if (request == null) {
            throw new ValidationException("Запрос на добавление нового фильма не может быть пустым");
        }
        if (request.getMpa() == null) {
            throw new NotFoundException("Mpa не может быть равно null");
        }
        mpaServiceBD.isExistsMpa(request.getMpa().getId());
    }

    private Set<Genre> validationGenres(Set<Genre> genres) {
        if (genres == null) {
            return new HashSet<>();
        }
        return genres.stream()
                .map(Genre::getId)
                .distinct()
                .map(genreServiceBD::getGenre)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private void validationId(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("id не может быть отрицательным, равными 0 или null");
        }
    }

    private Film validationFilm(Long idFilm) {
        Optional<Film> filmOpt = filmRepository.getFilm(idFilm);
        if (filmOpt.isEmpty()) {
            throw new NotFoundException("Фильм с id = " + idFilm + " в базе данных не найден");
        }
        return filmOpt.get();
    }
}
