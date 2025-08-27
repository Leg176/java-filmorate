package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    // Запрос на добавление друга
    public void addFriend(Long id, Long friendId) {
        if (getUser(id).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + id + " в списках зарегестрированных не найден");
        }
        if (getUser(friendId).isEmpty()) {
            throw new NotFoundException("Друг с id = " + friendId + " в списках зарегестрированных не найден");
        }
        if (id.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }
        User user1 = getUser(id).get();
        User user2 = getUser(friendId).get();
        user1.getFriendship().put(user2.getIdUser(), FriendshipStatus.PENDING);
        user2.getFriendship().put(user1.getIdUser(), FriendshipStatus.PENDING);
    }

    // Вывод всех друзей пользователя
    public List<User> findAllFriendsUser(Long id) {
        if (getUser(id).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + id + " в списках зарегестрированных не найден");
        }
        return getUser(id).get().getFriendship().entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .map(userStorage::getUser)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()
                );
    }

    // Метод для удаления друга
    public void removeFriend(Long id, Long friendId) {
        if (getUser(id).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + id + " в списках зарегестрированных не найден");
        }
        if (getUser(friendId).isEmpty()) {
            throw new NotFoundException("Друг с id = " + friendId + " в списках зарегестрированных не найден");
        }
        if (id.equals(friendId)) {
            throw new NotFoundException("Нельзя удалить самого себя из друзей");
        }
        User user1 = getUser(id).get();
        User user2 = getUser(friendId).get();
        user1.getFriendship().remove(user2.getIdUser());
        user2.getFriendship().remove(user1.getIdUser());
    }

    // Поиск общих друзей
    public List<User> findСommonFriendsUsers(Long id, Long otherId) {
        if (getUser(id).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + id + " в списках зарегестрированных не найден");
            }
        if (getUser(otherId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + otherId + " в списках зарегестрированных не найден");
        }
        List<User> friends1 = findAllFriendsUser(id);

        Set<Long> friendsIds1 = friends1.stream()
                .map(User::getIdUser)
                .collect(Collectors.toSet());

        return findAllFriendsUser(otherId).stream()
                .filter(friend -> friendsIds1.contains(friend.getIdUser()))
                .collect(Collectors.toList());
    }

    // Подтверждение дружбы
    public void confirmationOfFriendship(Long id, Long idFriends){
        if (getUser(id).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + id + " в списках зарегестрированных не найден");
        }
        if (getUser(idFriends).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + idFriends + " в списках зарегестрированных не найден");
        }
        if (getUser(id).get().getFriendship().containsKey(idFriends) &&
        getUser(idFriends).get().getFriendship().containsKey(id)) {
            getUser(id).get().getFriendship().put(idFriends, FriendshipStatus.CONFIRMED);
            getUser(idFriends).get().getFriendship().put(id, FriendshipStatus.CONFIRMED);
        }
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        log.info("Добавляем нового пользователя: {} в коллекцию.", user);
        isContainEmail(user);
        checkName(user);
        log.trace("Присваиваем пользователю уникальный id");
        user.setIdUser(getNextId());
        return userStorage.create(user);
    }

    public Optional<User> getUser(Long id) {
        if (userStorage.findAll() == null) {
            return Optional.empty();
        }
        return userStorage.getUser(id);
    }

    public User update(User newUser) {
        isContainEmail(newUser);
        checkName(newUser);
        log.trace("Обновление данных о пользователе");
        if (newUser.getIdUser() == null) {
            log.warn("Поле id должно быть заполненно");
            throw new ValidationException("Id должен быть указан");
        }
        return userStorage.update(newUser);
    }

    private long getNextId() {
        log.debug("Генерируем id для пользователей");
        long currentMaxId = userStorage.findAll()
                .stream()
                .map(User::getIdUser)
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void isContainEmail(User user) {
        log.trace("Проверка email {} на принадлежность другому пользователю", user.getEmail());
        boolean isContain = false;
        if (user.getIdUser() != null && (user.getIdUser() > 0 && getUser(user.getIdUser()).isPresent())) {
            User oldUser = getUser(user.getIdUser()).get();
            isContain = userStorage.findAll().stream()
                    .filter(u -> !u.equals(oldUser))
                    .anyMatch(u -> u.getEmail().equals(user.getEmail()));
        } else {
            isContain = userStorage.findAll().stream()
                    .anyMatch(u -> u.getEmail().equals(user.getEmail()));
        }
        if (isContain) {
            log.warn("Email {} используется другим пользователем", user.getEmail());
            throw new ValidationException("Этот имейл уже используется");
        }
    }

    private void checkName(User user) {
        log.trace("Проверка имени пользователя требованиям ТЗ");
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Пользователю присвоено имя: {}", user.getLogin());
        }
    }
}

