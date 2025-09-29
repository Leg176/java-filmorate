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

    private static final String FIND_ALL_QUERY = "SELECT * FROM genres";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genres WHERE id = ?";
    private static final String FIND_GENRES_BY_ID_FILM_QUERY = "SELECT g.* FROM genres g " +
            "INNER JOIN film_genres fg ON g.id = fg.genre_id WHERE fg.film_id = ?";
    private static final String FIND_BY_IDS_QUERY = "SELECT * FROM genres WHERE id IN (?)";
    private static final String FIND_ALL_GENRES_FOR_ALL_FILMS = "SELECT DISTINCT fg.film_id, g.id, " +
            "g.name FROM film_genres fg INNER JOIN genres g ON fg.genre_id = g.id WHERE fg.film_id " +
            "IN (%s) ORDER BY fg.film_id";

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
                                rs.getLong("film_id"),
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
