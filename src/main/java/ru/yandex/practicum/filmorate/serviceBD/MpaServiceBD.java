package ru.yandex.practicum.filmorate.serviceBD;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.MpaRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class MpaServiceBD {
    private final MpaRepository mpaRepository;

    @Autowired
    public MpaServiceBD(MpaRepository mpaRepository) {
        this.mpaRepository = mpaRepository;
    }

    public MotionPictureAssociation getMpa(Long idMpa) {
        if (idMpa <= 0) {
            throw new ValidationException("id не может быть отрицательными или равными 0");
        }
        if (mpaRepository.getMpa(idMpa).isEmpty()) {
            throw new NotFoundException("Рейтинг не обнаружен");
        }
        return mpaRepository.getMpa(idMpa).get();
    }

    public List<MotionPictureAssociation> getAllMpa() {
        return mpaRepository.getAllMpa();
    }

    public MotionPictureAssociation getMpaFilm(Long idFilm) {
        if (idFilm <= 0) {
            throw new ValidationException("id не может быть отрицательными или равными 0");
        }
        Optional<MotionPictureAssociation> mpaDto = mpaRepository.getFilmMpa(idFilm);
        if (mpaDto.isPresent()) {
            return mpaDto.get();
        } else {
            throw new NotFoundException("MPA фильма с id = " + idFilm + "не найден");
        }
    }

    public void isExistsMpa(Long idMpa) {
        if (mpaRepository.getMpa(idMpa).isEmpty()) {
            throw new NotFoundException("Mpa в базе данных не найдено");
        }
    }
}
