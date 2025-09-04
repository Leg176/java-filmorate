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
    private static final String INSERT_QUERY = "INSERT INTO Films(nameFilm, description, releaseDate, duration, " +
            "ratingMPA) VALUES (?, ?, ?, ?, ?) returning idFilm";
    private static final String UPDATE_QUERY = "UPDATE Films SET nameFilm = ?, description = ?, releaseDate = ?, " +
            "duration = ?, ratingMPA = ? WHERE idFilm = ?";
    private static final String FIND_TOP_FILMS_QUERY = "SELECT f.idFilm, f.nameFilm, f.description, f.releaseDate, " +
            "f.duration, f.ratingMPA FROM Films f WHERE f.idFilm IN (SELECT l.idFilm FROM Likes l GROUP BY l.idFilm " +
            "ORDER BY COUNT(l.idUser) DESC LIMIT ?)";
    private static final String FIND_LIKES_QUERY = "SELECT l.idUser FROM Likes l WHERE idFilm = ?";
    private static final String ADD_LIKE_QUERY = "INSERT INTO Likes(idFilm, idUser) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM Likes WHERE idFilm = ? AND idUser = ?";

    public FilmRepository(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    public void addLike(Long idFilm, Long idUser) {
        insert(ADD_LIKE_QUERY, idFilm, idUser);
    }

    public void deleteLike(Long idFilm, Long idUser) {
        jdbc.update(DELETE_LIKE_QUERY, idFilm, idUser);
    }

    public List<Long> findAllLikes(Long idFilm) {
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
        long idFilm = insert(
                INSERT_QUERY,
                film.getNameFilm(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getRating()
        );
        film.setIdFilm(idFilm);
        return film;
    }

    public Film update(Film film) {
        update(
                UPDATE_QUERY,
                film.getNameFilm(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getRating(),
                film.getIdFilm()
        );
        return film;
    }
}
