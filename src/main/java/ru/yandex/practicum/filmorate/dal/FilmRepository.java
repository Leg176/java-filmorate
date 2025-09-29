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
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE id = ?";
    private static final String FIND_BY_NAME_FILM_QUERY = "SELECT * FROM films WHERE name_film = ?";
    private static final String INSERT_QUERY =
            "INSERT INTO films(name_film, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY =
            "UPDATE films SET name_film = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
    private static final String FIND_TOP_FILMS_QUERY =
            "SELECT f.id, f.name_film, f.description, f.release_date, f.duration, f.mpa_id, " +
                    "mr.name_mpa AS mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa_rating mr ON f.mpa_id = mr.id " +
                    "LEFT JOIN (SELECT film_id, COUNT(user_id) AS counter FROM likes GROUP BY film_id " +
                    "ORDER BY COUNT(user_id) DESC) q ON q.film_id = f.id " +
                    "ORDER BY q.counter DESC LIMIT ?";
    private static final String FIND_LIKES_QUERY = "SELECT l.user_id FROM likes l WHERE film_id = ?";
    private static final String ADD_LIKE_QUERY = "MERGE INTO likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String DELETE_FILM_QUERY = "DELETE FROM films WHERE id = ?";
    private static final String LIKES_QUERY = "SELECT film_id, COUNT(*) as likesCount FROM likes WHERE film_id IN (";
    private static final String ADD_GENRE_QUERY = "MERGE INTO film_genres (film_id, genre_id) KEY (film_id, genre_id) " +
            "VALUES (?, ?)";
    private static final String DELETE_GENRE_QUERY = "DELETE FROM film_genres WHERE film_id = ? AND genre_id = ?";
    private static final String FIND_COMMON_FILMS = """
            SELECT сf.*
            FROM (SELECT f.*, mr.name_mpa
            	FROM likes l1
            	INNER JOIN films f ON f.id = l1.film_id
            	LEFT JOIN mpa_rating mr ON f.mpa_id = mr.id
            	INNER JOIN likes l2 ON l2.film_id = l1.film_id
                            AND l2.user_id = ?
                            WHERE l1.user_id = ?) сf
            	INNER JOIN (
            		SELECT l.film_id, count(l.user_id) AS cnt
                            FROM likes l
                            GROUP BY l.film_id) сl ON сl.film_id = сf.id
            ORDER BY сl.cnt desc
            """;
    private static final String ADD_DIRECTOR_QUERY = "MERGE INTO film_directors (idFilm, idDirector) " +
            "KEY (idFilm, idDirector) VALUES (?, ?)";
    private static final String DELETE_DIRECTOR_QUERY = "DELETE FROM film_directors WHERE idFilm = ? AND idDirector = ?";
    private static final String FIND_BY_DIRECTOR_QUERY = "SELECT f.* FROM films f JOIN film_directors fd ON " +
            "f.idFilm = fd.idFilm WHERE fd.idDirector = ?";
    private static final String FIND_BY_DIRECTOR_ORDER_BY_YEAR = """
            SELECT f.*
            FROM films f
            JOIN film_directors fd ON f.id = fd.idFilm
            WHERE fd.idDirector = ?
            ORDER BY f.release_date, f.id
            """;
    private static final String FIND_BY_DIRECTOR_ORDER_BY_LIKES = """
            SELECT f.*
            FROM films f
            JOIN film_directors fd ON f.id = fd.idFilm
            LEFT JOIN likes l ON l.film_id = f.id
            WHERE fd.idDirector = ?
            GROUP BY f.id, f.name_film, f.description, f.release_date, f.duration, f.mpa_id
            ORDER BY COUNT(l.user_id) DESC, f.id
            """;
    private static final String GET_RECOMMENDATION = """
            SELECT f.id, f.name_film, f.description, f.release_date, f.duration, f.mpa_id,
                   COUNT(*) AS score
            FROM likes l_sim
            JOIN likes l_cand ON l_cand.user_id = l_sim.user_id
            JOIN films f ON f.id = l_cand.film_id
            WHERE l_sim.film_id IN (SELECT film_id FROM likes WHERE user_id = ?)
              AND l_cand.film_id NOT IN (SELECT film_id FROM likes WHERE user_id = ?)
              AND l_cand.user_id <> ?
            GROUP BY f.id, f.name_film, f.description, f.release_date, f.duration, f.mpa_id
            ORDER BY score DESC, f.id
            """;
    private static final String FIND_MOST_POPULAR_TEMPLATE = """
              SELECT f.id, f.name_film, f.description, f.release_date, f.duration, f.mpa_id
              FROM films f
              LEFT JOIN likes l ON l.film_id = f.id
              LEFT JOIN film_genres fg ON fg.film_id = f.id
            """;
    private static final String SEARCH_BY_TITTLE_OR_DIRECTOR = """
                SELECT f.*, COUNT(l.film_id) as likes_count
                FROM films f
                LEFT JOIN likes l ON f.id = l.film_id
                LEFT JOIN film_directors fd ON f.id = fd.idFilm
                LEFT JOIN directors d ON fd.idDirector = d.id
                WHERE
                    (CASE
                        WHEN ? = 'title' THEN name_film ILIKE ?
                        WHEN ? = 'director' THEN d.name ILIKE ?
                        WHEN ? = 'title,director' THEN (name_film ILIKE ? OR d.name ILIKE ?)
                    END)
                GROUP BY f.id
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
        String query = LIKES_QUERY + placeholders + ") GROUP BY film_id";
        List<Object[]> likesData = jdbc.query(query, filmIds.toArray(new Long[0]),
                (rs, rowNum) -> new Object[]{
                        rs.getLong("film_id"),
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
                "id",
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
            cond.add("fg.genre_id = ?");
            params.add(genreId);
        }
        if (year != null) {
            cond.add("EXTRACT(YEAR FROM f.release_date) = ?");
            params.add(year);
        }

        StringBuilder sql = new StringBuilder(FIND_MOST_POPULAR_TEMPLATE);
        if (!cond.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", cond)).append(' ');
        }

        sql.append("GROUP BY f.id, f.name_film, f.description, f.release_date, f.duration, f.mpa_id ")
                .append("ORDER BY COUNT(l.user_id) DESC ")
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
