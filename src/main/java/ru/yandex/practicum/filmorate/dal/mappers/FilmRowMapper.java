package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmRowMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setIdFilm(resultSet.getLong("idFilm"));
        film.setNameFilm(resultSet.getString("nameFilm"));
        film.setDescription(resultSet.getString("description"));
        film.setReleaseDate(resultSet.getDate("releaseDate").toLocalDate());
        film.setDuration(resultSet.getObject("duration", Integer.class));
        MotionPictureAssociation mpa = new MotionPictureAssociation();
        mpa.setId(resultSet.getLong("idMpa"));
        film.setMpa(mpa);
        return film;
    }
}

