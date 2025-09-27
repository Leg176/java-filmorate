package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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
        users.put(user.getId(), user);
        log.info("Пользователь успешно добавлени с id: {}", user.getId());
        return user;
    }

    @Override
    public User update(User newUser) {
        log.info("Обновляем данные о пользователя с id {}.", newUser.getId());
        log.trace("Проверка наличия в коллекции пользователя с id указанным в теле метода PUT");
        if (users.containsKey(newUser.getId())) {
            users.put(newUser.getId(), newUser);
            log.info("Данные о пользователе {} обновлены", newUser);
            return newUser;
        }
        log.warn("Пользователь с id = {} не найден", newUser.getId());
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    @Override
    public Optional<User> getUser(Long id) {
        log.info("Вывод пользователя с id {}.", id);
        return Optional.ofNullable(users.get(id));
    }
}
