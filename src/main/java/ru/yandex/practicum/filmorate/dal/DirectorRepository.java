package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Repository
public class DirectorRepository extends BaseRepository<Director> {

    private static final String FIND_ALL_QUERY = "SELECT * FROM directors";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM directors WHERE id = ?";
    private static final String INSERT_QUERY = "INSERT INTO directors(name) VALUES (?)";
    private static final String UPDATE_QUERY = "UPDATE directors SET name = ? WHERE id = ?";
    private static final String DELETE_QUERY = "DELETE FROM directors WHERE id = ?";
    private static final String FIND_BY_FIRST_NAME_QUERY = "SELECT * FROM directors WHERE name = ?";
    private static final String FIND_BY_IDS_QUERY = "SELECT * FROM directors WHERE id IN (?)";
    private static final String FIND_ALL_DIRECTOR_FOR_ALL_FILMS = "SELECT DISTINCT fd.idFilm AS filmId, d.id, " +
            "d.name FROM film_directors fd INNER JOIN directors d ON fd.idDirector = d.id " +
            "WHERE fd.idFilm IN (%s) ORDER BY fd.idFilm";
    private static final String FIND_DIRECTORS_BY_ID_FILM_QUERY = "SELECT d.* FROM directors d " +
            "INNER JOIN film_directors fd ON d.id = fd.idDirector WHERE fd.idFilm = ?";

    public List<Director> findDirectorsFilm(long idFilm) {
        return findMany(FIND_DIRECTORS_BY_ID_FILM_QUERY, idFilm);
    }

    public DirectorRepository(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper);
    }

    public Optional<Director> findByFirstName(String firstName) {
        return findOne(FIND_BY_FIRST_NAME_QUERY, firstName);
    }

    public List<Director> getAllDirector() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Director> findById(Long idDirector) {
        return findOne(FIND_BY_ID_QUERY, idDirector);
    }

    public void deleteDirector(Long idDirector) {
        jdbc.update(DELETE_QUERY, idDirector);
    }

    public Director save(Director director) {
        long id = insert(INSERT_QUERY, "id",
                director.getName()
        );
        director.setId(id);
        return director;
    }

    public Director update(Director director) {
        update(UPDATE_QUERY,
                director.getName(),
                director.getId()
        );
        return director;
    }

    public List<Director> findAllByIdInOrderById(Set<Long> set) {
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

    public Map<Long, Set<Director>> findDirectorByFilmIds(List<Long> filmIds) {

        if (filmIds == null || filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String placeholders = IntStream.range(0, filmIds.size())
                .mapToObj(i -> "?")
                .collect(Collectors.joining(", "));

        String sql = String.format(FIND_ALL_DIRECTOR_FOR_ALL_FILMS, placeholders);

        return jdbc.query(
                        sql,
                        filmIds.toArray(),
                        (rs, rowNum) -> new Object[]{
                                rs.getLong("filmId"),
                                mapper.mapRow(rs, rowNum)
                        }
                ).stream()
                .collect(Collectors.groupingBy(
                        arr -> (Long) arr[0],
                        Collectors.mapping(
                                arr -> (Director) arr[1],
                                Collectors.toSet()
                        )
                ));
    }
}