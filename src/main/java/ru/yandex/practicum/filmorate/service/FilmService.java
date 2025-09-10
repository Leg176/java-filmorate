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

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    private final MpaRepository mpaRepository;
    private final GenreRepository genreRepository;

    private static final LocalDate FIRST_FILM_DATE = LocalDate.of(1895, 12, 25);

    @Autowired
    public FilmService(FilmRepository filmRepository, GenreRepository genreRepository,
                       UserRepository userRepository, MpaRepository mpaRepository) {
        this.filmRepository = filmRepository;
        this.userRepository = userRepository;
        this.mpaRepository = mpaRepository;
        this.genreRepository = genreRepository;
    }

    public FilmDto createFilm(NewFilmRequest request) {
        if (request == null) {
            throw new ValidationException("Запрос на добавление нового фильма не может быть пустым");
        }
        checkReleaseDate(request.getReleaseDate());
        MotionPictureAssociation mpaRequest = request.getMpa();
        if (mpaRequest == null) {
            throw new ValidationException("Mpa в запросе на обновление данных фильма не может быть пустым");
        }
        Optional<MotionPictureAssociation> mpaOpt = mpaRepository.getMpa(mpaRequest.getId());
        if (mpaOpt.isEmpty()) {
            throw new NotFoundException("Mpa с id = " + mpaRequest.getId() + " в базе данных не найден");
        }
        //Лишние действие
        MotionPictureAssociation mpaBD = mpaOpt.get();
        Set<Genre> genres = request.getGenres();
        Set<Genre> validGenres = validationGenres(genres);

        Film film = FilmMapper.mapToFilm(request);
        //Лишние действие
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
        Optional<Film> filmOpt = filmRepository.getFilm(idFilm);
        if (filmOpt.isEmpty()) {
            throw new NotFoundException("Фильм с id = " + idFilm + " в базе данных не найден");
        }
        Film film = filmOpt.get();
        Optional<MotionPictureAssociation> mpaOpt = mpaRepository.getMpa(film.getIdMpa());
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

            // Проверяем, что список фильмов не пустой
            if (films.isEmpty()) {
                return Collections.emptyList();
            }

            // Собираем уникальные ID для MPA
            List<Long> ids = films.stream()
                    .map(Film::getIdFilm)
                    .distinct()
                    .collect(Collectors.toList());

            // Загружаем MPA и жанры
            Map<Long, MotionPictureAssociation> mpaMap = mpaRepository.findMpaByFilmIds(ids);
            Map<Long, Set<Genre>> genreMap = genreRepository.findGenresByFilmIds(ids);

            // Связываем данные
            for (Film film : films) {
                film.setMpa(mpaMap.get(film.getIdFilm()));
                film.setGenres(genreMap.getOrDefault(film.getIdFilm(), new HashSet<>()));
            }

            // Преобразуем в DTO
            return films.stream()
                    .map(FilmMapper::mapToFilmDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Ошибка при получении списка фильмов", e);
            throw new RuntimeException("Ошибка при получении списка фильмов", e);
        }
    }

    public FilmDto updateFilm(UpdateFilmRequest request) {
        if (request == null) {
            throw new ValidationException("Запрос на обновление данных фильма не может быть пустым");
        }
        if (request.hasReleaseDate()) {
            checkReleaseDate(request.getReleaseDate());
        }
        // Получаем объект по id запроса
        Film film = validationFilm(request.getId());
        //Находим жанры принадлежавшие старому объекту Film
        Optional<MotionPictureAssociation> mpaOpt = mpaRepository.getMpa(film.getIdMpa());
        if (mpaOpt.isEmpty()) {
            throw new NotFoundException("Mpa для фильма в базе данных с id = " + film.getIdFilm() + "не найден");
        }
        MotionPictureAssociation mpa = mpaOpt.get();
        List<Genre> genresOldFilm = genreRepository.findGenresFilm(film.getIdFilm());
        if (genresOldFilm.isEmpty()) {
            throw new NotFoundException("Жанры для фильма в базе данных с id " + film.getIdFilm() + " не обнаружен");
        }
        MotionPictureAssociation mpaRequest = request.getMpa();
        if (mpaRequest == null) {
            throw new ValidationException("Mpa в запросе на обновление данных фильма не может быть пустым");
        }
        //Создаём новый объект
        Film newFilm = FilmMapper.updateFilmFields(film, request);
        //проверяем Genre/Mpa
        Optional<MotionPictureAssociation> mpaBD = mpaRepository.getMpa(mpaRequest.getId());
        if (mpaBD.isEmpty()) {
            throw new NotFoundException("Mpa в базе данных не найдено");
        }
        Set<Genre> validGenreUpdate = validationGenres(newFilm.getGenres());
        newFilm.setGenres(validGenreUpdate);
        filmRepository.update(newFilm);
        // Удаляем старые связи
        deleteGenreBD(film, genresOldFilm);
        //Добавляем в таблицу связей пары фильм - жанр/рейтинг
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
    }

    public void deleteLikes(Long idFilm, Long idUser) {
        validationInLikes(idFilm, idUser);
        filmRepository.deleteLike(idFilm, idUser);
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

    private Set<Genre> validationGenres(Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return new HashSet<>();
        }

        // Извлекаем все ID жанров
        Set<Long> genreIds = genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        // Получаем все жанры одним запросом
        List<Genre> foundGenres = genreRepository.findAllByIdInOrderById(genreIds);

        // Создаем мапу для быстрого поиска
        Map<Long, Genre> genreMap = foundGenres.stream()
                .collect(Collectors.toMap(Genre::getId, Function.identity()));

        // Проверяем наличие всех жанров и собираем результат
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