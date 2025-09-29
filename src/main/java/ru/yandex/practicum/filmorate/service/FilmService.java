package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.*;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;
import ru.yandex.practicum.filmorate.model.enums.EventType;

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
    private final DirectorRepository directorRepository;
    private final EventService eventService;

    private static final LocalDate FIRST_FILM_DATE = LocalDate.of(1895, 12, 25);

    @Autowired
    public FilmService(FilmRepository filmRepository, GenreRepository genreRepository,
                       UserRepository userRepository, MpaRepository mpaRepository,
                       DirectorRepository directorRepository, EventService eventService) {
        this.filmRepository = filmRepository;
        this.userRepository = userRepository;
        this.mpaRepository = mpaRepository;
        this.genreRepository = genreRepository;
        this.directorRepository = directorRepository;
        this.eventService = eventService;
    }

    public List<FilmDto> getFilmsByDirector(Long directorId, String sortBy) {
        Optional<Director> director = directorRepository.findById(directorId);
        if (director.isEmpty()) {
            throw new NotFoundException("Режиссёр с id = " + directorId + " не найден");
        }

        // Получение фильмов режиссера
        List<Film> films = filmRepository.findFilmsByDirector(directorId, sortBy);
        fillingTheObjectFields(films);

        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public FilmDto createFilm(NewFilmRequest request) {
        checkReleaseDate(request.getReleaseDate());

        MotionPictureAssociation mpaRequest = request.getMpa();
        MotionPictureAssociation mpaBD = validateAndGetMpa(mpaRequest);

        Set<Genre> genres = request.getGenres();
        Set<Genre> validGenres = validationGenres(genres);

        Set<Director> directors = request.getDirectors();
        Set<Director> validDirectors = validationDirector(directors);

        Film film = FilmMapper.mapToFilm(request);
        film.setMpa(mpaBD);
        film.setGenres(new HashSet<>(validGenres));
        film.setDirector(new HashSet<>(validDirectors));

        filmRepository.save(film);
        log.info("Фильм сохранен с ID: {}", film.getIdFilm());
        for (Genre genre : film.getGenres()) {
            addGenres(film.getIdFilm(), genre.getId());
        }
        for (Director director : film.getDirector()) {
            Long id = director.getId();
            addDirector(film.getIdFilm(), id);
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
        List<Director> directors = directorRepository.findDirectorsFilm(idFilm);
        film.setDirector(new HashSet<>(directors));
        return FilmMapper.mapToFilmDto(film);
    }

    public List<FilmDto> getFilms() {
        try {
            List<Film> films = filmRepository.findAll();
            if (films.isEmpty()) {
                return Collections.emptyList();
            }
            fillingTheObjectFields(films);

            return films.stream()
                    .map(FilmMapper::mapToFilmDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Ошибка при получении списка фильмов", e);
            throw new RuntimeException("Ошибка при получении списка фильмов", e);
        }
    }

    public FilmDto updateFilm(UpdateFilmRequest request) {
        if (request.hasReleaseDate()) {
            checkReleaseDate(request.getReleaseDate());
        }
        // Получаем объект по id запроса
        Film film = validationFilm(request.getId());
        MotionPictureAssociation mpa = validateAndGetMpa(request.getMpa());
        //Находим жанры принадлежавшие старому объекту Film
        List<Genre> genresOldFilm = genreRepository.findGenresFilm(film.getIdFilm());
        //Находим режиссёров принадлежавших старому объекту Film
        List<Director> directorOldFilm = directorRepository.findDirectorsFilm(film.getIdFilm());
        //Создаём новый объект
        Film newFilm = FilmMapper.updateFilmFields(film, request);
        Set<Genre> validGenreUpdate = validationGenres(newFilm.getGenres());
        newFilm.setGenres(validGenreUpdate);
        Set<Director> validDirectorUpdate = validationDirector(newFilm.getDirector());
        newFilm.setDirector(validDirectorUpdate);
        newFilm.setMpa(mpa);
        deleteGenreBD(film, genresOldFilm);
        deleteDirectorBD(film, directorOldFilm);
        filmRepository.update(newFilm);
        // Удаляем старые связи
        if (validGenreUpdate != null && !validGenreUpdate.isEmpty()) {
            for (Genre g : validGenreUpdate) {
                addGenres(newFilm.getIdFilm(), g.getId());
            }
        }
        if (validDirectorUpdate != null && !validDirectorUpdate.isEmpty()) {
            for (Director director : validDirectorUpdate) {
                Long id = director.getId();
                addDirector(newFilm.getIdFilm(), id);
            }
        }
        return FilmMapper.mapToFilmDto(newFilm);
    }

    public List<FilmDto> topFilms(int count) {
        List<Film> films = filmRepository.findTopFilm(count);
        fillingTheObjectFields(films);
        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> getMostPopular(Integer count, Long genreId, Integer year) {
        int limit = (count == null || count <= 0) ? 10 : count;
        List<Film> films = filmRepository.findMostPopular(limit, genreId, year);
        if (films.isEmpty()) {
            return Collections.emptyList();
        }
        fillingTheObjectFields(films);

        return films.stream().map(FilmMapper::mapToFilmDto).toList();
    }

    public void addLikes(Long idFilm, Long idUser) {
        validationInLikes(idFilm, idUser);
        filmRepository.addLike(idFilm, idUser);
        eventService.add(idFilm, idUser, EventType.LIKE);
    }

    public void deleteLikes(Long idFilm, Long idUser) {
        validationInLikes(idFilm, idUser);
        filmRepository.deleteLike(idFilm, idUser);
        eventService.delete(idFilm, idUser, EventType.LIKE);

    }

    private void addDirector(Long idFilm, Long idDirector) {
        filmRepository.addDirector(idFilm, idDirector);
    }

    private void deleteDirectorBD(Film film, List<Director> directors) {
        directors.stream()
                .map(Director::getId)
                .forEach(id -> filmRepository.delDirector(film.getIdFilm(), id));
    }

    private void addGenres(Long idFilm, Long idGenre) {
        filmRepository.addGenre(idFilm, idGenre);
    }

    private void deleteGenreBD(Film film, List<Genre> genres) {
        genres.stream()
                .map(Genre::getId)
                .forEach(id -> filmRepository.delGenre(film.getIdFilm(), id));
    }

    public void deleteFilm(Long idFilm) {
        validationFilm(idFilm);
        filmRepository.deleteFilm(idFilm);
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
        return filmRepository.getFilm(idFilm)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + idFilm + " в базе данных не найден"));
    }

    private MotionPictureAssociation validateAndGetMpa(MotionPictureAssociation mpa) {
        if (mpa == null) {
            throw new ValidationException("Mpa в запросе не может быть пустым");
        }
        return mpaRepository.getMpa(mpa.getId())
                .orElseThrow(() -> new NotFoundException("Mpa с id = " + mpa.getId() + " не найден"));
    }

    private Set<Director> validationDirector(Set<Director> directors) {
        if (directors == null || directors.isEmpty()) {
            return new HashSet<>();
        }
        // Извлекаем все ID режиссёров
        Set<Long> directorIds = directors.stream()
                .map(Director::getId)
                .collect(Collectors.toSet());
        // Получаем всех режиссёров одним запросом
        List<Director> foundDirectors = directorRepository.findAllByIdInOrderById(directorIds);
        // Создаем мапу для быстрого поиска
        Map<Long, Director> directorMap = foundDirectors.stream()
                .collect(Collectors.toMap(Director::getId, Function.identity()));
        // Проверяем наличие всех режиссёров и собираем результат
        return directors.stream()
                .map(Director::getId)
                .distinct()
                .map(id -> {
                    Director director = directorMap.get(id);
                    if (director == null) {
                        throw new NotFoundException("Режиссёр с ID " + id + " не найден");
                    }
                    return director;
                })
                .collect(Collectors.toSet());
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

    public List<FilmDto> getCommon(Long userId, Long friendId) {
        List<Film> commonFilms = filmRepository.getCommon(userId, friendId);
        fillingTheObjectFields(commonFilms);

        return commonFilms.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> searchFilms(String query, String by) {
        log.info("Поиск фильмов по запросу '{}', параметр поиска: {}", query, by);

        // Проверка корректности параметра поиска
        Set<String> validSearchParameters = Set.of("title", "director", "title,director", "director,title");
        if (!validSearchParameters.contains(by)) {
            throw new IllegalArgumentException("Неверный параметр поиска. Допустимые значения: 'title', 'director'," +
                    " 'title,director'");
        }

        List<Film> films = filmRepository.searchByTitleOrDirector(by, query);
        fillingTheObjectFields(films);

        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    private void fillingTheObjectFields(List<Film> films) {
        List<Long> filmIds = films.stream()
                .map(Film::getIdFilm)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, MotionPictureAssociation> mpaMap = mpaRepository.findMpaByFilmIds(filmIds);
        Map<Long, Set<Genre>> genreMap = genreRepository.findGenresByFilmIds(filmIds);
        Map<Long, Set<Director>> directorMap = directorRepository.findDirectorByFilmIds(filmIds);

        for (Film film : films) {
            film.setMpa(mpaMap.getOrDefault(film.getIdFilm(), null));
            film.setGenres(genreMap.getOrDefault(film.getIdFilm(), new HashSet<>()));
            film.setDirector(directorMap.getOrDefault(film.getIdFilm(), new HashSet<>()));
        }
    }
}