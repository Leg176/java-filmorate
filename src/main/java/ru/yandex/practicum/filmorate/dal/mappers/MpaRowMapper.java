package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MpaRowMapper implements RowMapper<MotionPictureAssociation> {
    @Override
    public MotionPictureAssociation mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        MotionPictureAssociation mpa = new MotionPictureAssociation();
        mpa.setId(resultSet.getLong("idMpa"));
        mpa.setName(resultSet.getString("nameMpa"));
        return mpa;
    }
}
