package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.dal.MpaRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.Operation;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    private final MpaRepository mpaRepository;
    private final GenreRepository genreRepository;
    private final FeedService feedService;

    private static final LocalDate FIRST_FILM_DATE = LocalDate.of(1895, 12, 25);

    @Autowired
    public FilmService(FilmRepository filmRepository,
                       GenreRepository genreRepository,
                       UserRepository userRepository,
                       MpaRepository mpaRepository,
                       FeedService feedService) {
        this.filmRepository = filmRepository;
        this.userRepository = userRepository;
        this.mpaRepository = mpaRepository;
        this.genreRepository = genreRepository;
        this.feedService = feedService;
    }

    public FilmDto createFilm(NewFilmRequest request) {
        validateRequest(request, "Запрос на добавление нового фильма не может быть пустым");
        checkReleaseDate(request.getReleaseDate());

        MotionPictureAssociation mpaRequest = request.getMpa();
        MotionPictureAssociation mpaBD = validateAndGetMpa(mpaRequest);

        Set<Genre> genres = request.getGenres();
        Set<Genre> validGenres = validationGenres(genres);

        Film film = FilmMapper.mapToFilm(request);
        film.setMpa(mpaBD);
        film.setGenres(new HashSet<>(validGenres));

        filmRepository.save(film);
        log.info("Фильм сохранен с ID: {}", film.getIdFilm());
        for (Genre genre : film.getGenres()) {
            Long id = genre.getId();
            addGenres(film.getIdFilm(), id);
        }
        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto getFilmById(Long idFilm) {
        Film film = validationFilm(idFilm);
        Optional<MotionPictureAssociation> mpaOpt = mpaRepository.getMpa(film.getMpa().getId());
        if (mpaOpt.isEmpty()) {
            throw new NotFoundException("Mpa в базе данных не найдено");
        }
        film.setMpa(mpaOpt.get());
        List<Genre> genres = genreRepository.findGenresFilm(idFilm);
        film.setGenres(new HashSet<>(genres));
        return FilmMapper.mapToFilmDto(film);
    }

    public List<FilmDto> getFilms() {
        try {
            List<Film> films = filmRepository.findAll();
            if (films.isEmpty()) {
                return Collections.emptyList();
            }

            List<Long> ids = films.stream()
                    .map(Film::getIdFilm)
                    .distinct()
                    .collect(Collectors.toList());

            Map<Long, MotionPictureAssociation> mpaMap = mpaRepository.findMpaByFilmIds(ids);
            Map<Long, Set<Genre>> genreMap = genreRepository.findGenresByFilmIds(ids);

            for (Film film : films) {
                film.setMpa(mpaMap.get(film.getIdFilm()));
                film.setGenres(genreMap.getOrDefault(film.getIdFilm(), new HashSet<>()));
            }

            return films.stream()
                    .map(FilmMapper::mapToFilmDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Ошибка при получении списка фильмов", e);
            throw new RuntimeException("Ошибка при получении списка фильмов", e);
        }
    }

    public FilmDto updateFilm(UpdateFilmRequest request) {
        validateRequest(request, "Запрос на обновление данных фильма не может быть пустым");
        if (request.hasReleaseDate()) {
            checkReleaseDate(request.getReleaseDate());
        }
        Film film = validationFilm(request.getId());
        MotionPictureAssociation mpaRequest = request.getMpa();
        MotionPictureAssociation mpa = validateAndGetMpa(mpaRequest);
        List<Genre> genresOldFilm = genreRepository.findGenresFilm(film.getIdFilm());
        if (genresOldFilm.isEmpty()) {
            throw new NotFoundException("Жанры для фильма в базе данных с id " + film.getIdFilm() + " не обнаружен");
        }
        Film newFilm = FilmMapper.updateFilmFields(film, request);
        Set<Genre> validGenreUpdate = validationGenres(newFilm.getGenres());
        newFilm.setGenres(validGenreUpdate);
        newFilm.setMpa(mpa);
        filmRepository.update(newFilm);
        deleteGenreBD(film, genresOldFilm);
        for (Genre genre : validGenreUpdate) {
            Long id = genre.getId();
            addGenres(newFilm.getIdFilm(), id);
        }
        return FilmMapper.mapToFilmDto(newFilm);
    }

    public List<FilmDto> topFilms(int count) {
        return filmRepository.findTopFilm(count).stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public void addLikes(Long idFilm, Long idUser) {
        validationInLikes(idFilm, idUser);
        filmRepository.addLike(idFilm, idUser);
        feedService.recordEvent(idUser, EventType.LIKE, Operation.ADD, idFilm);
    }

    public void deleteLikes(Long idFilm, Long idUser) {
        validationInLikes(idFilm, idUser);
        filmRepository.deleteLike(idFilm, idUser);
        feedService.recordEvent(idUser, EventType.LIKE, Operation.REMOVE, idFilm);
    }

    private void addGenres(Long idFilm, Long idGenre) {
        filmRepository.addGenre(idFilm, idGenre);
    }

    private void deleteGenreBD(Film film, List<Genre> genres) {
        genres.stream()
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
        if (userRepository.getUser(idUser).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + idUser + " в списках зарегестрированных не найден");
        }
        validationFilm(idFilm);
    }

    private Film validationFilm(Long idFilm) {
        Optional<Film> filmOpt = filmRepository.getFilm(idFilm);
        if (filmOpt.isEmpty()) {
            throw new NotFoundException("Фильм с id = " + idFilm + " в базе данных не найден");
        }
        return filmOpt.get();
    }

    private void validateRequest(Object request, String message) {
        if (request == null) {
            throw new ValidationException(message);
        }
    }

    private MotionPictureAssociation validateAndGetMpa(MotionPictureAssociation mpa) {
        validateRequest(mpa, "Mpa в запросе не может быть пустым");
        Optional<MotionPictureAssociation> mpaOpt = mpaRepository.getMpa(mpa.getId());
        if (mpaOpt.isEmpty()) {
            throw new NotFoundException("Mpa с id = " + mpa.getId() + " не найден");
        }
        return mpaOpt.get();
    }

    private Set<Genre> validationGenres(Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return new HashSet<>();
        }

        Set<Long> genreIds = genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        List<Genre> foundGenres = genreRepository.findAllByIdInOrderById(genreIds);

        Map<Long, Genre> genreMap = foundGenres.stream()
                .collect(Collectors.toMap(Genre::getId, Function.identity()));

        return genres.stream()
                .map(Genre::getId)
                .distinct()
                .map(id -> {
                    Genre genre = genreMap.get(id);
                    if (genre == null) {
                        throw new NotFoundException("Жанр с ID " + id + " не найден");
                    }
                    return genre;
                })
                .collect(Collectors.toSet());
    }
}
