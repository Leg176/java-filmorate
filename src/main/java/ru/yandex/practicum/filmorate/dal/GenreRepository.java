package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Repository
public class GenreRepository extends BaseRepository<Genre> {

    private static final String FIND_ALL_QUERY = "SELECT * FROM Genres";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM Genres WHERE idGenre = ?";
    private static final String FIND_GENRES_BY_ID_FILM_QUERY = "SELECT g.* FROM Genres g " +
            "INNER JOIN FilmGenres fg ON g.idGenre = fg.idGenre WHERE fg.idFilm = ?";
    private static final String FIND_BY_IDS_QUERY = "SELECT * FROM Genres WHERE idGenre IN (?)";
    private static final String FIND_ALL_GENRES_FOR_ALL_FILMS = "SELECT DISTINCT fg.idFilm, g.idGenre, " +
            "g.name FROM FilmGenres fg INNER JOIN Genres g ON fg.idGenre = g.idGenre WHERE fg.idFilm " +
            "IN (%s) ORDER BY fg.idFilm";

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

    public List<Genre> findAllByIdInOrderById(Set<Long> set) {
        if (set == null || set.isEmpty()) {
            return Collections.emptyList();
        }

        String query = buildInQuery(set.size());
        Object[] params = set.toArray();

        return findMany(query, params);
    }

    private String buildInQuery(int size) {
        String placeholders = IntStream.range(0, size)
                .mapToObj(i -> "?")
                .collect(Collectors.joining(", "));

        return FIND_BY_IDS_QUERY.replace("(?)", "(" + placeholders + ")");
    }

    public Map<Long, Set<Genre>> findGenresByFilmIds(List<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String placeholders = IntStream.range(0, filmIds.size())
                .mapToObj(i -> "?")
                .collect(Collectors.joining(", "));

        String sql = String.format(FIND_ALL_GENRES_FOR_ALL_FILMS, placeholders);

        return jdbc.query(
                        sql,
                        filmIds.toArray(),
                        (rs, rowNum) -> new Object[]{
                                rs.getLong("idFilm"),
                                mapper.mapRow(rs, rowNum)
                        }
                ).stream()
                .collect(Collectors.groupingBy(
                        arr -> (Long) arr[0],
                        Collectors.mapping(
                                arr -> (Genre) arr[1],
                                Collectors.toSet()
                        )
                ));
    }
}
