package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

@Repository
public class FilmRepository extends BaseRepository<Film> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM Films";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM Films WHERE idFilm = ?";
    private static final String FIND_BY_NAME_FILM_QUERY = "SELECT * FROM Films WHERE nameFilm = ?";
    private static final String INSERT_QUERY = "INSERT INTO Films(nameFilm, description, releaseDate, duration, idMpa) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE Films SET nameFilm = ?, description = ?, releaseDate = ?, " +
            "duration = ?, idMpa = ? WHERE idFilm = ?";
    private static final String FIND_TOP_FILMS_QUERY = "SELECT f.idFilm, f.nameFilm, f.description, f.releaseDate,"
            + " f.duration, f.idMpa, mr.nameMpa AS mpa_name FROM Films f LEFT JOIN Mpa_rating mr ON f.idMpa = mr.idMpa"
            + " LEFT JOIN (SELECT idFilm, COUNT(idUser) AS counter FROM Likes GROUP BY idFilm"
            + " ORDER BY COUNT(idUser) DESC) q ON q.idFilm = f.idFilm ORDER BY q.counter DESC LIMIT ?";
    private static final String FIND_LIKES_QUERY = "SELECT l.idUser FROM Likes l WHERE idFilm = ?";
    private static final String ADD_LIKE_QUERY = "INSERT INTO Likes(idFilm, idUser) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM Likes WHERE idFilm = ? AND idUser = ?";
    private static final String DELETE_FILM_QUERY = "DELETE FROM Films WHERE idFilm = ?";
    private static final String ADD_GENRE_QUERY = "INSERT INTO FilmGenres(idFilm, idGenre) VALUES (?, ?)";
    private static final String DELETE_GENRE_QUERY = "DELETE FROM FilmGenres WHERE idFilm = ? AND idGenre = ?";

    public FilmRepository(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
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
        long id = insert(INSERT_QUERY, "idFilm",
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
}
