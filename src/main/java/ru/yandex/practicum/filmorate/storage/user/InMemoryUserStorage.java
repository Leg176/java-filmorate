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
        log.info("Добавляем нового пользователя: {} в коллекцию.", user);
        isContainEmail(user);

        log.trace("Проверка имени пользователя требованиям ТЗ");
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Пользователю присвоено имя: {}", user.getLogin());
        }

        log.trace("Присваиваем пользователю уникальный id");
        user.setId(getNextId());
        // сохраняем новую публикацию в памяти приложения
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
            User oldUser = users.get(newUser.getId());
            isContainEmail(newUser);

            log.trace("Обновление email пользователя");
            oldUser.setEmail(newUser.getEmail());

            log.trace("Обновление login пользователя");
            oldUser.setLogin(newUser.getLogin());

            log.trace("Обновление имени пользователя");
            if (newUser.getName() != null) {
                oldUser.setName(newUser.getName());
            }

            log.trace("Обновление даты рождения пользователя");
            oldUser.setBirthday(newUser.getBirthday());

            log.info("Данные о пользователе {} обновлены", oldUser);
            return oldUser;
        }
        log.warn("Пользователь с id = {} не найден", newUser.getId());
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    @Override
    public Optional<User> getUser(Long id) {
        log.info("Вывод пользователя с id {}.", id);
        if (users == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(users.get(id));
    }

    private long getNextId() {
        log.debug("Генерируем id для пользователей");
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void isContainEmail(User user) {
        log.trace("Проверка email {} на принадлежность другому пользователю", user.getEmail());
        boolean isContain = false;
        if (user.getId() != null && (user.getId() > 0 && getUser(user.getId()).isPresent())) {
            User oldUser = getUser(user.getId()).get();
            isContain = users.values().stream()
                    .filter(u -> !u.equals(oldUser))
                    .anyMatch(u -> u.getEmail().equals(user.getEmail()));
        } else {
            isContain = users.values().stream()
                    .anyMatch(u -> u.getEmail().equals(user.getEmail()));
        }
        if (isContain) {
            log.warn("Email {} используется другим пользователем", user.getEmail());
            throw new ValidationException("Этот имейл уже используется");
        }
    }
}
