package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class MpaRepository extends BaseRepository<MotionPictureAssociation> {

    private static final String FIND_ALL_QUERY = "SELECT * FROM Mpa_rating";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM Mpa_rating WHERE idMpa = ?";
    private static final String FIND_MPA_FILM = "SELECT mr.idMpa, mr.nameMpa FROM FilmMpa fm " +
            "INNER JOIN Mpa_rating mr ON fm.idMpa = mr.idMpa WHERE fm.idFilm = ?";

    public MpaRepository(JdbcTemplate jdbc, RowMapper<MotionPictureAssociation> mapper) {
        super(jdbc, mapper);
    }

    public Optional<MotionPictureAssociation> getFilmMpa(Long idFilm) {
        return findOne(FIND_MPA_FILM, idFilm);
    }

    public List<MotionPictureAssociation> getAllMpa() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<MotionPictureAssociation> getMpa(Long idMpa) {
        return findOne(FIND_BY_ID_QUERY, idMpa);
    }
}
