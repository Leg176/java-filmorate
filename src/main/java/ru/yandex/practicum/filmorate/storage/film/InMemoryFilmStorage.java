package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> findAll() {
        log.info("Получаем полный список фильмов содержащихся в коллекции");
        return films.values();
    }

    @Override
    public Film create(Film film) {
        log.debug("Сохраняем фильм в коллекцию");
        films.put(film.getIdFilm(), film);
        log.info("Фильм успешно добавлени с id: {}", film.getIdFilm());
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        log.info("Обновляем данные о фильме с id: {}.", newFilm.getIdFilm());
        log.trace("Проверка наличия в коллекции фильма с id указанным в теле метода PUT");
        if (films.containsKey(newFilm.getIdFilm())) {
            films.put(newFilm.getIdFilm(), newFilm);
            log.info("Данные о фильме {} обновлены", newFilm);
            return newFilm;
        }
        log.warn("Фильм с id = {} не найден", newFilm.getIdFilm());
        throw new NotFoundException("Фильм с id = " + newFilm.getIdFilm() + " не найден");
    }

    @Override
    public Optional<Film> getFilm(Long id) {
        log.info("Вывод фильма с id {}.", id);
        return Optional.ofNullable(films.get(id));
    }
}
