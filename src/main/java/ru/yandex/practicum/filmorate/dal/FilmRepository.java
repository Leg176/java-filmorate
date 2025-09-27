package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Repository
public class FilmRepository extends BaseRepository<Film> {

    public FilmRepository(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE idFilm = ?";
    private static final String FIND_BY_NAME_FILM_QUERY = "SELECT * FROM films WHERE nameFilm = ?";

    private static final String INSERT_QUERY =
            "INSERT INTO films(nameFilm, description, releaseDate, duration, idMpa) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_QUERY =
            "UPDATE films SET nameFilm = ?, description = ?, releaseDate = ?, duration = ?, idMpa = ? WHERE idFilm = ?";

    private static final String FIND_TOP_FILMS_QUERY =
            "SELECT f.idFilm, f.nameFilm, f.description, f.releaseDate, f.duration, f.idMpa, " +
                    "       mr.nameMpa AS mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa_rating mr ON f.idMpa = mr.idMpa " +
                    "LEFT JOIN (SELECT idFilm, COUNT(idUser) AS counter FROM likes GROUP BY idFilm " +
                    "            ORDER BY COUNT(idUser) DESC) q ON q.idFilm = f.idFilm " +
                    "ORDER BY q.counter DESC LIMIT ?";

    private static final String FIND_LIKES_QUERY = "SELECT l.idUser FROM likes l WHERE idFilm = ?";
    private static final String ADD_LIKE_QUERY = "MERGE INTO likes (idFilm, idUser) KEY (idFilm, idUser) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM likes WHERE idFilm = ? AND idUser = ?";
    private static final String DELETE_FILM_QUERY = "DELETE FROM films WHERE idFilm = ?";
    private static final String LIKES_QUERY = "SELECT idFilm, COUNT(*) as likesCount FROM likes WHERE idFilm IN (";
    private static final String ADD_GENRE_QUERY = "MERGE INTO film_genres (idFilm, idGenre) KEY (idFilm, idGenre) " +
            "VALUES (?, ?)";
    private static final String DELETE_GENRE_QUERY = "DELETE FROM film_genres WHERE idFilm = ? AND idGenre = ?";
    private static final String FIND_COMMON_FILMS = """
            SELECT сf.*
            FROM (SELECT f.*, mr.namempa
            	FROM likes l1
            	INNER JOIN films f ON f.idFilm = l1.idFilm
            	LEFT JOIN mpa_rating mr ON f.idMpa = mr.idMpa
            	INNER JOIN likes l2 ON l2.idFilm = l1.idFilm
                            AND l2.idUser = ?
                            WHERE l1.idUser = ?) сf
            	INNER JOIN (
            		SELECT l.idFilm, count(l.idUser) AS cnt
                            FROM likes l
                            GROUP BY l.idFilm) сl ON сl.idFilm = сf.idFilm
            ORDER BY сl.cnt desc
            """;

    private static final String ADD_DIRECTOR_QUERY = "MERGE INTO film_directors (idFilm, idDirector) " +
            "KEY (idFilm, idDirector) VALUES (?, ?)";
    private static final String DELETE_DIRECTOR_QUERY = "DELETE FROM film_directors WHERE idFilm = ? AND idDirector = ?";

    private static final String FIND_BY_DIRECTOR_QUERY = "SELECT f.* FROM films f JOIN film_directors fd ON " +
            "f.idFilm = fd.idFilm WHERE fd.idDirector = ?";
    /* ▼▼▼ NEW: сортировка на уровне SQL ▼▼▼ */
    private static final String FIND_BY_DIRECTOR_ORDER_BY_YEAR = """
            SELECT f.*
            FROM films f
            JOIN film_directors fd ON f.idFilm = fd.idFilm
            WHERE fd.idDirector = ?
            ORDER BY f.releaseDate, f.idFilm
            """;

    private static final String FIND_BY_DIRECTOR_ORDER_BY_LIKES = """
            SELECT f.*
            FROM films f
            JOIN film_directors fd ON f.idFilm = fd.idFilm
            LEFT JOIN likes l ON l.idFilm = f.idFilm
            WHERE fd.idDirector = ?
            GROUP BY f.idFilm, f.nameFilm, f.description, f.releaseDate, f.duration, f.idMpa
            ORDER BY COUNT(l.idUser) DESC, f.idFilm
            """;
    /* ▲▲▲ NEW ▲▲▲ */

    /* ▼▼▼ NEW: рекомендации без глубоких вложенных IN ▼▼▼ */
    private static final String GET_RECOMMENDATION = """
            SELECT f.idFilm, f.nameFilm, f.description, f.releaseDate, f.duration, f.idMpa,
                   COUNT(*) AS score
            FROM likes l_sim
            JOIN likes l_cand ON l_cand.idUser = l_sim.idUser
            JOIN films f ON f.idFilm     = l_cand.idFilm
            WHERE l_sim.idFilm IN (SELECT idFilm FROM likes WHERE idUser = ?)
              AND l_cand.idFilm NOT IN (SELECT idFilm FROM likes WHERE idUser = ?)
              AND l_cand.idUser <> ?
            GROUP BY f.idFilm, f.nameFilm, f.description, f.releaseDate, f.duration, f.idMpa
            ORDER BY score DESC, f.idFilm
            """;
    /* ▲▲▲ NEW ▲▲▲ */

    private static final String FIND_MOST_POPULAR_TEMPLATE = """
              SELECT f.idFilm, f.nameFilm, f.description, f.releaseDate, f.duration, f.idMpa
              FROM films f
              LEFT JOIN likes l ON l.idFilm = f.idFilm
              LEFT JOIN film_genres fg ON fg.idFilm = f.idFilm
            """;

    private static final String SEARCH_BY_TITTLE_OR_DIRECTOR = """
                SELECT f.*, COUNT(l.idFilm) as likes_count
                FROM films f
                LEFT JOIN likes l ON f.idFilm = l.idFilm
                LEFT JOIN film_directors fd ON f.idFilm = fd.idFilm
                LEFT JOIN directors d ON fd.idDirector = d.idDirector
                WHERE
                    (CASE
                        WHEN ? = 'title' THEN nameFilm ILIKE ?
                        WHEN ? = 'director' THEN d.name ILIKE ?
                        WHEN ? = 'title,director' THEN (nameFilm ILIKE ? OR d.name ILIKE ?)
                    END)
                GROUP BY f.idFilm
                ORDER BY likes_count DESC
            """;

    public List<Film> findFilmsByDirector(Long directorId, String sortBy) {
        if ("likes".equalsIgnoreCase(sortBy)) {
            // сортировка по количеству лайков в SQL
            return findMany(FIND_BY_DIRECTOR_ORDER_BY_LIKES, directorId);
        }
        // по умолчанию сортируем по году релиза в SQL
        return findMany(FIND_BY_DIRECTOR_ORDER_BY_YEAR, directorId);
    }

    public void countLikesForFilms(List<Film> films) {
        if (films.isEmpty()) return;
        List<Long> filmIds = films.stream()
                .map(Film::getIdFilm)
                .toList();
        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String query = LIKES_QUERY + placeholders + ") GROUP BY idFilm";
        List<Object[]> likesData = jdbc.query(query, filmIds.toArray(new Long[0]),
                (rs, rowNum) -> new Object[]{
                        rs.getLong("idFilm"),
                        rs.getLong("likesCount")
                });
        Map<Long, Long> likesMap = new HashMap<>();
        for (Object[] row : likesData) {
            likesMap.put((Long) row[0], (Long) row[1]);
        }
        for (Film film : films) {
            film.setLikes(likesMap.getOrDefault(film.getIdFilm(), 0L));
        }
    }

    public void addDirector(Long idFilm, Long idDirector) {
        jdbc.update(ADD_DIRECTOR_QUERY, idFilm, idDirector);
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
        jdbc.update(ADD_GENRE_QUERY, idFilm, idGenre);
    }

    public void addLike(Long idFilm, Long idUser) {
        jdbc.update(ADD_LIKE_QUERY, idFilm, idUser);
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
        List<String> cond = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (genreId != null) {
            cond.add("fg.idGenre = ?");
            params.add(genreId);
        }
        if (year != null) {
            cond.add("EXTRACT(YEAR FROM f.releaseDate) = ?");
            params.add(year);
        }

        StringBuilder sql = new StringBuilder(FIND_MOST_POPULAR_TEMPLATE);
        if (!cond.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", cond)).append(' ');
        }

        sql.append("GROUP BY f.idFilm, f.nameFilm, f.description, f.releaseDate, f.duration, f.idMpa ")
                .append("ORDER BY COUNT(l.idUser) DESC ")
                .append("LIMIT ?");

        params.add(limit);
        return findMany(sql.toString(), params.toArray());
    }

    public List<Film> searchByTitleOrDirector(String by, String query) {
        return findMany(SEARCH_BY_TITTLE_OR_DIRECTOR, by, "%" + query + "%",
                by, "%" + query + "%",
                by, "%" + query + "%",
                "%" + query + "%");
    }

    public List<Film> getRecommendations(Long userId) {
        return findMany(GET_RECOMMENDATION, userId, userId, userId);
    }
}
