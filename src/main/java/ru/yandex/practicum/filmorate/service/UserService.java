package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    // Добавление друга
    public void addFriend(User user1, User user2) {
        user1.getFriends().add(user2.getId());
        user2.getFriends().add(user1.getId());
    }

    // Вывод всех друзей пользователя
    public List<User> findAllFriendsUser(User user) {
        return user.getFriends().stream()
                .map(userStorage::getUser)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()
                );
    }

    // Метод для удаления друга
    public void removeFriend(User user1, User user2) {
        user1.getFriends().remove(user2.getId());
        user2.getFriends().remove(user1.getId());
    }

    // Поиск общих друзей
    public List<User> findСommonFriendsUsers(User user1, User user2) {
        return findAllFriendsUser(user1).stream()
                .filter(friend -> findAllFriendsUser(user2).contains(friend))
                .collect(Collectors.toList());
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public Optional<User> getUser(Long id) {
        return userStorage.getUser(id);
    }

    public User update(User newUser) {
        log.trace("Обновление данных о пользователе");
        if (newUser.getId() == null) {
            log.warn("Поле id должно быть заполненно");
            throw new ValidationException("Id должен быть указан");
        }
        return userStorage.update(newUser);
    }
}

