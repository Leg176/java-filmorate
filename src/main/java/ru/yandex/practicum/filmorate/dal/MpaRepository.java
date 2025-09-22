package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class MpaRepository extends BaseRepository<MotionPictureAssociation> {

    private static final String FIND_ALL_QUERY = "SELECT * FROM Mpa_rating";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM Mpa_rating WHERE idMpa = ?";
    private static final String FIND_ALL_MPA_FOR_ALL_FILMS = "SELECT f.idFilm AS filmId, m.idMpa, m.nameMpa " +
        "FROM Films f INNER JOIN Mpa_rating m ON f.idMpa = m.idMpa WHERE f.idFilm IN (%s) ORDER BY f.idFilm";

    public MpaRepository(JdbcTemplate jdbc, RowMapper<MotionPictureAssociation> mapper) {
        super(jdbc, mapper);
    }

    public List<MotionPictureAssociation> getAllMpa() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<MotionPictureAssociation> getMpa(Long idMpa) {
        return findOne(FIND_BY_ID_QUERY, idMpa);
    }

    public Map<Long, MotionPictureAssociation> findMpaByFilmIds(List<Long> filmIds) {

        if (filmIds == null || filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String placeholders = StringUtils.arrayToCommaDelimitedString(
                new String[filmIds.size()]).replace("null", "?");
        String sql = String.format(FIND_ALL_MPA_FOR_ALL_FILMS, placeholders);

        List<Object[]> results = jdbc.query(
                sql,
                filmIds.toArray(),
                (rs, rowNum) -> new Object[] {
                        rs.getLong("filmId"),
                        new MpaRowMapper().mapRow(rs, rowNum)
                }
        );

        return results.stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (MotionPictureAssociation) arr[1]
                ));
    }
}
