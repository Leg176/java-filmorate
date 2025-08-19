package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        log.info("Получаем полный список пользователей содержащихся в коллекции");
        return users.values();
    }

    @Override
    public User create(User user) {
        log.debug("Сохраняем пользователя в коллекцию");
        users.put(user.getIdUser(), user);
        log.info("Пользователь успешно добавлени с id: {}", user.getIdUser());
        return user;
    }

    @Override
    public User update(User newUser) {
        log.info("Обновляем данные о пользователя с id {}.", newUser.getIdUser());
        log.trace("Проверка наличия в коллекции пользователя с id указанным в теле метода PUT");
        if (users.containsKey(newUser.getIdUser())) {
            users.put(newUser.getIdUser(), newUser);
            log.info("Данные о пользователе {} обновлены", newUser);
            return newUser;
        }
        log.warn("Пользователь с id = {} не найден", newUser.getIdUser());
        throw new NotFoundException("Пользователь с id = " + newUser.getIdUser() + " не найден");
    }

    @Override
    public Optional<User> getUser(Long id) {
        log.info("Вывод пользователя с id {}.", id);
        return Optional.ofNullable(users.get(id));
    }
}
