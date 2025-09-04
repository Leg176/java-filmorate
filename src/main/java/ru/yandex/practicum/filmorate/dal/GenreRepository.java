package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Repository
public class GenreRepository extends BaseRepository<Genre>{
    private static final String FIND_ALL_QUERY = "SELECT * FROM Genres";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM Genres WHERE idGenre = ?";
    private static final String FIND_GENRES_BY_ID_FILM_QUERY = "SELECT g.* FROM Genres g " +
            "INNER JOIN FilmGenres fg ON g.idGenre = fg.idGenre WHERE idFilm = ?";

    public GenreRepository(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    public List<Genre> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Genre> getGenre(long idGenre) {
        return findOne(FIND_BY_ID_QUERY, idGenre);
    }

    public List<Genre> findGenresFilm(long idFilm) {
        return findMany(FIND_GENRES_BY_ID_FILM_QUERY, idFilm);
    }
}
