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
        log.info("Добавляем новый фильм {} в коллекцию.", film);

        log.trace("Проверка даты релиза фильма на соблюдение требования ТЗ");
        if (!checkReleaseDate(film.getReleaseDate())) {
            log.warn("Дата выхода: {} фильма не должна быть ранее 25.12.1895 года", film.getDuration());
            throw new ValidationException("Дата выпуска фильма должна быть позже 25.12.1895г.");
        }
        log.trace("Присваиваем фильму уникальный id");
        film.setId(getNextId());
        // сохраняем новую публикацию в памяти приложения
        log.debug("Сохраняем фильм в коллекцию");
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлени с id: {}", film.getId());
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        log.trace("Обновление данных о фильме");
        if (newFilm.getId() == null) {
            log.warn("Не указан id фильма");
            throw new ValidationException("Id должен быть указан");
        }

        if (!checkReleaseDate(newFilm.getReleaseDate())) {
            log.warn("Дата выхода: {} фильма не должна быть ранее 25.12.1895 года", newFilm.getDuration());
            throw new ValidationException("Дата выпуска фильма должна быть позже 25.12.1895г.");
        }
        log.info("Обновляем данные о фильме с id: {}.", newFilm.getId());
        log.trace("Проверка наличия в коллекции фильма с id указанным в теле метода PUT");
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());

            log.trace("Обновляем название фильма.");
            oldFilm.setName(newFilm.getName());

            log.trace("Обновляем описание фильма.");
            oldFilm.setDescription(newFilm.getDescription());

            log.trace("Обновляем дату выхода фильма.");
            oldFilm.setReleaseDate(newFilm.getReleaseDate());

            log.trace("Обновляем продолжительность фильма.");
            if (newFilm.getDuration() > 0) {
                oldFilm.setDuration(newFilm.getDuration());
            }

            log.info("Данные о фильме {} обновлены", oldFilm);
            return oldFilm;
        }

        log.warn("Фильм с id = {} не найден", newFilm.getId());
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    @Override
    public Optional<Film> getFilm(Long id) {
        if (films == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(films.get(id));
    }

    private long getNextId() {
        log.debug("Генерируем id для фильма");
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private boolean checkReleaseDate(LocalDate localDate) {
        log.debug("Проверяем дату выхода фильма");
        LocalDate firstFilmDate = LocalDate.of(1895, 12, 25);
        return localDate.isAfter(firstFilmDate);
    }
}
