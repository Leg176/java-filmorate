package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;
import ru.yandex.practicum.filmorate.serviceBD.GenreServiceBD;
import ru.yandex.practicum.filmorate.serviceBD.MpaServiceBD;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

@Component
public class FilmRowMapper implements RowMapper<Film> {

    private final GenreServiceBD genreServiceBD;
    private final MpaServiceBD mpaServiceBD;

    @Autowired
    public FilmRowMapper(GenreServiceBD genreServiceBD, MpaServiceBD mpaServiceBD) {
        this.genreServiceBD = genreServiceBD;
        this.mpaServiceBD = mpaServiceBD;
    }

    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setIdFilm(resultSet.getLong("idFilm"));
        film.setNameFilm(resultSet.getString("nameFilm"));
        film.setDescription(resultSet.getString("description"));
        film.setReleaseDate(resultSet.getDate("releaseDate").toLocalDate());
        film.setDuration(resultSet.getObject("duration", Integer.class));
        MotionPictureAssociation mpa = mpaServiceBD.getMpaFilm(film.getIdFilm());
        film.setMpa(mpa);
        List<Genre> genres = genreServiceBD.getGenresByIdFilm(film.getIdFilm());
        film.setGenres(new HashSet<>(genres));
        return film;
    }
}

