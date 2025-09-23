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
import ru.yandex.practicum.filmorate.model.*;

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

    private static final LocalDate FIRST_FILM_DATE = LocalDate.of(1895, 12, 25);

    @Autowired
    public FilmService(FilmRepository filmRepository, GenreRepository genreRepository,
                       UserRepository userRepository, MpaRepository mpaRepository,
                       DirectorRepository directorRepository) {
        this.filmRepository = filmRepository;
        this.userRepository = userRepository;
        this.mpaRepository = mpaRepository;
        this.genreRepository = genreRepository;
        this.directorRepository = directorRepository;
    }

    public List<FilmDto> getFilmsByDirector(Long directorId, String sortBy) {
        if (directorId == null) {
            throw new IllegalArgumentException("Id не может быть равно null");
        }
        // Получение фильмов режиссера
        List<Film> films = filmRepository.findFilmsByDirector(directorId, sortBy);

        List<Long> ids = films.stream()
                .map(Film::getIdFilm)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, MotionPictureAssociation> mpaMap = mpaRepository.findMpaByFilmIds(ids);
        Map<Long, Set<Director>> directorMap = directorRepository.findDirectorByFilmIds(ids);
        Map<Long, Set<Genre>> genreMap = genreRepository.findGenresByFilmIds(ids);

        for (Film film : films) {
            film.setMpa(mpaMap.get(film.getIdFilm()));
            film.setDirector(directorMap.getOrDefault(film.getIdFilm(), new HashSet<>()));
            film.setGenres(genreMap.getOrDefault(film.getIdFilm(), new HashSet<>()));
        }
        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public FilmDto createFilm(NewFilmRequest request) {
        validateRequest(request, "Запрос на добавление нового фильма не может быть пустым");
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

            List<Long> ids = films.stream()
                    .map(Film::getIdFilm)
                    .distinct()
                    .collect(Collectors.toList());

            Map<Long, MotionPictureAssociation> mpaMap = mpaRepository.findMpaByFilmIds(ids);
            Map<Long, Set<Director>> directorMap = directorRepository.findDirectorByFilmIds(ids);
            Map<Long, Set<Genre>> genreMap = genreRepository.findGenresByFilmIds(ids);

            for (Film film : films) {
                film.setMpa(mpaMap.get(film.getIdFilm()));
                film.setDirector(directorMap.getOrDefault(film.getIdFilm(), new HashSet<>()));
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
        // Получаем объект по id запроса
        Film film = validationFilm(request.getId());
        MotionPictureAssociation mpa = validateAndGetMpa(request.getMpa());

        //Находим жанры принадлежавшие старому объекту Film
        List<Genre> genresOldFilm = genreRepository.findGenresFilm(film.getIdFilm());
        if (genresOldFilm.isEmpty()) {
            throw new NotFoundException("Жанры для фильма в базе данных с id " + film.getIdFilm() + " не обнаружен");
        }
        //Находим режиссёров принадлежавших старому объекту Film
        List<Director> directorOldFilm = directorRepository.findDirectorsFilm(film.getIdFilm());
        //Создаём новый объект
        Film newFilm = FilmMapper.updateFilmFields(film, request);
        Set<Genre> validGenreUpdate = validationGenres(newFilm.getGenres());
        newFilm.setGenres(validGenreUpdate);
        Set<Director> validDirectorUpdate = validationDirector(newFilm.getDirector());
        newFilm.setDirector(validDirectorUpdate);
        newFilm.setMpa(mpa);

        filmRepository.update(newFilm);
        // Удаляем старые связи
        deleteGenreBD(film, genresOldFilm);
        //Добавляем в таблицу связей пары фильм - жанр
        for (Genre g : validGenreUpdate) {
            addGenres(newFilm.getIdFilm(), g.getId());
        }
        //Добавляем в таблицу связей пары фильм - режиссёр
        for (Director director : validDirectorUpdate) {
            Long id = director.getId();
            addDirector(newFilm.getIdFilm(), id);
        }
        return FilmMapper.mapToFilmDto(newFilm);
    }

    public List<FilmDto> topFilms(int count) {
        return filmRepository.findTopFilm(count).stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> getMostPopular(Integer count, Long genreId, Integer year) {
        int limit = (count == null || count <= 0) ? 10 : count;
        List<Film> films = filmRepository.findMostPopular(limit, genreId, year);
        if (films.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> ids = films.stream().map(Film::getIdFilm).distinct().toList();
        Map<Long, MotionPictureAssociation> mpaMap = mpaRepository.findMpaByFilmIds(ids);
        Map<Long, Set<Genre>> genreMap = genreRepository.findGenresByFilmIds(ids);
        for (Film f : films) {
            f.setMpa(mpaMap.get(f.getIdFilm()));
            f.setGenres(genreMap.getOrDefault(f.getIdFilm(), new HashSet<>()));
        }
        return films.stream().map(FilmMapper::mapToFilmDto).toList();
    }

    public void addLikes(Long idFilm, Long idUser) {
        validationInLikes(idFilm, idUser);
        filmRepository.addLike(idFilm, idUser);
    }

    public void deleteLikes(Long idFilm, Long idUser) {
        validationInLikes(idFilm, idUser);
        filmRepository.deleteLike(idFilm, idUser);
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

    private void validateRequest(Object request, String message) {
        if (request == null) {
            throw new ValidationException(message);
        }
    }

    private MotionPictureAssociation validateAndGetMpa(MotionPictureAssociation mpa) {
        validateRequest(mpa, "Mpa в запросе не может быть пустым");
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
        // Проверяем наличие всех жанров и собираем результат
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
        List<Long> ids = commonFilms.stream()
                .map(Film::getIdFilm)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, MotionPictureAssociation> mpaMap = mpaRepository.findMpaByFilmIds(ids);
        Map<Long, Set<Genre>> genreMap = genreRepository.findGenresByFilmIds(ids);

        for (Film film : commonFilms) {
            film.setMpa(mpaMap.get(film.getIdFilm()));
            film.setGenres(genreMap.getOrDefault(film.getIdFilm(), new HashSet<>()));
        }

        return commonFilms.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    /**
     * Метод для получения рекомендаций фильмов для пользователя
     * @param userId ID пользователя, для которого формируем рекомендации
     * @return Список рекомендуемых фильмов
     */
    public List<FilmDto> getRecommendations(Long userId) {
        // Проверяем, существует ли пользователь
        validationUser(userId);

        // Получаем фильмы, которые пользователь уже оценил (поставил лайк)
        Set<Long> likedFilms = new HashSet<>(filmRepository.findAllLikesFilm(userId));

        // Если пользователь еще не оценил ни один фильм, возвращаем пустой список
        if (likedFilms.isEmpty()) {
            return Collections.emptyList();
        }

        // Найдем пользователей с похожими предпочтениями
        List<Long> similarUsers = findSimilarUsers(userId, likedFilms);

        // Если нет пользователей с похожими предпочтениями, возвращаем пустой список
        if (similarUsers.isEmpty()) {
            return Collections.emptyList();
        }

        // Найдем фильмы, которые понравились похожим пользователям, но еще не понравились текущему
        Set<Long> recommendedFilmIds = findCommonLikedFilms(similarUsers, likedFilms);

        // Если подходящих фильмов нет, возвращаем пустой список
        if (recommendedFilmIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Получаем информацию о рекомендуемых фильмах из репозитория
        List<Film> recommendedFilms = filmRepository.findMany(
                "SELECT * FROM Films WHERE idFilm IN (" +
                        String.join(",", recommendedFilmIds.stream().map(String::valueOf).collect(Collectors.toList())) + ")",
                recommendedFilmIds.toArray()
        );

        // Добавляем информацию о жанрах, рейтинге и режиссерах к каждому фильму
        List<Long> filmIds = recommendedFilms.stream().map(Film::getIdFilm).collect(Collectors.toList());

        Map<Long, MotionPictureAssociation> mpaMap = mpaRepository.findMpaByFilmIds(filmIds);
        Map<Long, Set<Genre>> genreMap = genreRepository.findGenresByFilmIds(filmIds);
        Map<Long, Set<Director>> directorMap = directorRepository.findDirectorByFilmIds(filmIds);

        for (Film film : recommendedFilms) {
            film.setMpa(mpaRepository.getFilmMpa(film.getIdFilm()));
            film.setGenres(genreRepository.getFilmGenres(film.getIdFilm()));
            film.setDirector(directorRepository.getFilmDirectors(film.getIdFilm()));
        }

        // Возвращаем DTO-объекты фильмов
        return recommendedFilms.stream()
                .filter(film -> film.getNameFilm() != null && !film.getNameFilm().isEmpty())
                .filter(film -> film.getDescription() != null && !film.getDescription().isEmpty())
                .filter(film -> film.getReleaseDate() != null)
                .filter(film -> film.getDuration() != null)
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    /**
     * Проверяет существование пользователя
     * @param userId ID пользователя
     */
    private void validationUser(Long userId) {
        if (!userRepository.getUser(userId).isPresent()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

    /**
     * Находит пользователей с похожими предпочтениями
     * @param userId ID текущего пользователя
     * @param likedFilms Фильмы, которые пользователь уже оценил
     * @return Список ID пользователей с похожими предпочтениями
     */
    private List<Long> findSimilarUsers(Long userId, Set<Long> likedFilms) {
        // Получаем всех пользователей
        List<User> allUsers = userRepository.findAll();

        // Если нет других пользователей, возвращаем пустой список
        if (allUsers.size() <= 1) {
            return Collections.emptyList();
        }

        // Для каждого пользователя вычисляем коэффициент сходства
        Map<Long, Integer> similarityScores = new HashMap<>();

        for (User user : allUsers) {
            if (!user.getIdUser().equals(userId)) {
                // Получаем фильмы, которые оценил текущий пользователь
                Set<Long> userLikedFilms = new HashSet<>(filmRepository.findAllLikesFilm(user.getIdUser()));

                // Вычисляем пересечение между фильмами текущего пользователя и другого
                Set<Long> commonFilms = new HashSet<>(likedFilms);
                commonFilms.retainAll(userLikedFilms);

                // Чем больше общих фильмов, тем выше коэффициент сходства
                int score = commonFilms.size();
                similarityScores.put(user.getIdUser(), score);
            }
        }

        // Сортируем пользователей по коэффициенту сходства (от большего к меньшему)
        return similarityScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Находит фильмы, которые понравились похожим пользователям, но еще не понравились текущему
     * @param similarUsers Список ID пользователей с похожими предпочтениями
     * @param likedFilms Фильмы, которые пользователь уже оценил
     * @return Список ID рекомендуемых фильмов
     */
    private Set<Long> findCommonLikedFilms(List<Long> similarUsers, Set<Long> likedFilms) {
        Set<Long> recommendedFilmIds = new HashSet<>();

        // Для каждого похожего пользователя находим фильмы, которые он оценил, но текущий пользователь еще не оценил
        for (Long similarUserId : similarUsers) {
            Set<Long> userLikedFilms = new HashSet<>(filmRepository.findAllLikesFilm(similarUserId));
            userLikedFilms.removeAll(likedFilms);

            // Добавляем эти фильмы в список рекомендаций
            recommendedFilmIds.addAll(userLikedFilms);
        }

        return recommendedFilmIds;
    }
}