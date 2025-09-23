package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class FilmRepository extends BaseRepository<Film> {


    public FilmRepository(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    private static final String FIND_ALL_QUERY = "SELECT * FROM Films";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM Films WHERE idFilm = ?";
    private static final String FIND_BY_NAME_FILM_QUERY = "SELECT * FROM Films WHERE nameFilm = ?";

    private static final String INSERT_QUERY =
            "INSERT INTO Films(nameFilm, description, releaseDate, duration, idMpa) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_QUERY =
            "UPDATE Films SET nameFilm = ?, description = ?, releaseDate = ?, duration = ?, idMpa = ? WHERE idFilm = ?";

    private static final String FIND_TOP_FILMS_QUERY =
            "SELECT f.idFilm, f.nameFilm, f.description, f.releaseDate, f.duration, f.idMpa, " +
                    "       mr.nameMpa AS mpa_name " +
                    "FROM Films f " +
                    "LEFT JOIN Mpa_rating mr ON f.idMpa = mr.idMpa " +
                    "INNER JOIN (SELECT idFilm, COUNT(idUser) AS counter FROM Likes GROUP BY idFilm " +
                    "            ORDER BY COUNT(idUser) DESC) q ON q.idFilm = f.idFilm " +
                    "ORDER BY q.counter DESC LIMIT ?";

    private static final String FIND_LIKES_QUERY = "SELECT l.idUser FROM Likes l WHERE idFilm = ?";
    private static final String ADD_LIKE_QUERY = "INSERT INTO Likes(idFilm, idUser) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM Likes WHERE idFilm = ? AND idUser = ?";
    private static final String DELETE_FILM_QUERY = "DELETE FROM Films WHERE idFilm = ?";
    private static final String LIKES_QUERY = "SELECT idFilm, COUNT(*) as likesCount FROM Likes WHERE idFilm IN (";
    private static final String ADD_GENRE_QUERY = "INSERT INTO FilmGenres(idFilm, idGenre) VALUES (?, ?)";
    private static final String DELETE_GENRE_QUERY = "DELETE FROM FilmGenres WHERE idFilm = ? AND idGenre = ?";
    private static final String FIND_COMMON_FILMS = """
            SELECT сf.*
            FROM (SELECT f.*, mr.namempa
            	FROM likes l1
            	INNER JOIN films f ON f.idFilm = l1.idFilm
            	LEFT JOIN Mpa_rating mr ON f.idMpa = mr.idMpa
            	INNER JOIN likes l2 ON l2.idFilm = l1.idFilm
                            AND l2.idUser = ?
                            WHERE l1.idUser = ?) сf
            	INNER JOIN (
            		SELECT l.idFilm, count(l.idUser) AS cnt
                            FROM likes l
                            GROUP BY l.idFilm) сl ON сl.idFilm = сf.idFilm
            ORDER BY сl.cnt desc
            """;
    private static final String ADD_DIRECTOR_QUERY = "INSERT INTO FilmDirectors(idFilm, idDirector) VALUES (?, ?)";
    private static final String DELETE_DIRECTOR_QUERY = "DELETE FROM FilmDirectors WHERE idFilm = ? AND idDirector = ?";
    private static final String FIND_BY_DIRECTOR_QUERY = "SELECT f.* FROM Films f JOIN FilmDirectors fd ON " +
            "f.idFilm = fd.idFilm WHERE fd.idDirector = ?";

    public List<Film> findFilmsByDirector(Long directorId, String sortBy) {

        List<Film> films = findMany(FIND_BY_DIRECTOR_QUERY, directorId);
        countLikesForFilms(films);

        return films.stream()
                .sorted((f1, f2) -> {
                    if ("year".equalsIgnoreCase(sortBy)) {
                        return f1.getReleaseDate().compareTo(f2.getReleaseDate());
                    } else if ("likes".equalsIgnoreCase(sortBy)) {
                        return Long.compare(f2.getLikes(), f1.getLikes()); // DESC
                    } else {
                        return f1.getReleaseDate().compareTo(f2.getReleaseDate()); // по умолчанию
                    }
                })
                .collect(Collectors.toList());
    }

    public void countLikesForFilms(List<Film> films) {
        if (films.isEmpty()) return;
        // Собираем все id фильмов
        List<Long> filmIds = films.stream()
                .map(Film::getIdFilm)
                .toList();
        // Получаем все лайки для этих фильмов
        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String query = LIKES_QUERY + placeholders + ") GROUP BY idFilm";
        // Получаем все лайки
        List<Object[]> likesData = jdbc.query(query, filmIds.toArray(new Long[0]),
                (rs, rowNum) -> new Object[]{
                        rs.getLong("idFilm"),
                        rs.getLong("likesCount")
                });
        // Создаем мапу лайков
        Map<Long, Long> likesMap = new HashMap<>();
        for (Object[] row : likesData) {
            likesMap.put((Long) row[0], (Long) row[1]);
        }
        // Заполняем лайки в объектах Film
        for (Film film : films) {
            film.setLikes(likesMap.getOrDefault(film.getIdFilm(), 0L));
        }
    }

    public void addDirector(Long idFilm, Long idDirector) {
        insert(ADD_DIRECTOR_QUERY, "idFilm", idFilm, idDirector);
    }

    public void delDirector(Long idFilm, Long idDirector) {
        jdbc.update(DELETE_DIRECTOR_QUERY, idFilm, idDirector);
    }

    public void deleteFilm(long idFilm) {
        jdbc.update(DELETE_FILM_QUERY, idFilm);
    }

    public void delGenre(Long idFilm, Long idGenre) {
        jdbc.update(DELETE_GENRE_QUERY, idFilm, idGenre);
    }

    public void addGenre(Long idFilm, Long idGenre) {
        insert(ADD_GENRE_QUERY, "idFilm", idFilm, idGenre);
    }

    public void addLike(Long idFilm, Long idUser) {
        insert(ADD_LIKE_QUERY, "idFilm", idFilm, idUser);
    }

    public void deleteLike(Long idFilm, Long idUser) {
        jdbc.update(DELETE_LIKE_QUERY, idFilm, idUser);
    }

    public List<Long> findAllLikesFilm(Long idFilm) {
        return jdbc.queryForList(FIND_LIKES_QUERY, Long.class, idFilm);
    }

    public List<Film> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public List<Film> findTopFilm(int quantity) {
        return findMany(FIND_TOP_FILMS_QUERY, quantity);
    }

    public Optional<Film> getFilm(long idFilm) {
        return findOne(FIND_BY_ID_QUERY, idFilm);
    }

    public Optional<Film> findByNameFilm(String nameFilm) {
        return findOne(FIND_BY_NAME_FILM_QUERY, nameFilm);
    }

    public Film save(Film film) {
        long id = insert(
                INSERT_QUERY,
                "idFilm",
                film.getNameFilm(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId()
        );
        film.setIdFilm(id);
        return film;
    }

    public Film update(Film film) {
        update(
                UPDATE_QUERY,
                film.getNameFilm(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getIdFilm()
        );
        return film;
    }

    public List<Film> getCommon(Long userId, Long friendId) {
        return findMany(FIND_COMMON_FILMS, userId, friendId);
    }

    public List<Film> findMostPopular(int limit, Long genreId, Integer year) {
        StringBuilder sql = new StringBuilder(
                "SELECT f.idFilm, f.nameFilm, f.description, f.releaseDate, f.duration, f.idMpa " +
                        "FROM Films f " +
                        "LEFT JOIN Likes l ON l.idFilm = f.idFilm " +
                        "LEFT JOIN FilmGenres fg ON fg.idFilm = f.idFilm " +
                        "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (genreId != null) {
            sql.append("AND fg.idGenre = ? ");
            params.add(genreId);
        }
        if (year != null) {
            sql.append("AND EXTRACT(YEAR FROM f.releaseDate) = ? ");
            params.add(year);
        }

        sql.append("GROUP BY f.idFilm ")
                .append("ORDER BY COUNT(l.idUser) DESC ")
                .append("LIMIT ?");

        params.add(limit);

        return findMany(sql.toString(), params.toArray());
    }

    public List<Film> searchByTitle(String query) {
        String sql = "SELECT * FROM Films WHERE nameFilm ILIKE ?";
        return findMany(sql, "%" + query + "%");
    }

    public List<Film> searchByDirector(String query) {
        String sql = """
            SELECT DISTINCT f.* 
            FROM Films f
            JOIN FilmDirectors fd ON f.idFilm = fd.idFilm
            JOIN Directors d ON fd.idDirector = d.idDirector
            WHERE d.name ILIKE ?
            """;
        return findMany(sql, "%" + query + "%");
    }

    public Long getLikeCount(Long filmId) {
        String sql = "SELECT COUNT(*) FROM Likes WHERE idFilm = ?";
        return jdbc.queryForObject(sql, Long.class, filmId);
    }
}