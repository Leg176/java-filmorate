package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {

    // Вывод всех пользователей содержащихся в коллекции
    Collection<User> findAll();

    // Добавление нового пользователя в коллекцию
    User create(User user);

    // Обновления данных о пользователе в коллекции
    User update(User newUser);

    // Вывод пользователя по его id
    Optional<User> getUser(Long id);
}
