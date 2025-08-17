package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
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
        user1.getFriends().put(user2.getIdUser(), FriendshipStatus.UNCONFIRMED);
        user2.getFriends().put(user1.getIdUser(), FriendshipStatus.UNCONFIRMED);
        confirmationOfFriendship(id, friendId);
    }

    // Вывод всех друзей пользователя
    public List<User> findAllFriendsUser(Long id) {
        if (getUser(id).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + id + " в списках зарегестрированных не найден");
        }
        return getUser(id).get().getFriends().keySet().stream()
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
        user1.getFriends().remove(user2.getIdUser());
        user2.getFriends().remove(user1.getIdUser());
    }

    // Поиск общих друзей
    public List<User> findСommonFriendsUsers(Long id, Long otherId) {
        if (getUser(id).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + id + " в списках зарегестрированных не найден");
            }
        if (getUser(otherId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + otherId + " в списках зарегестрированных не найден");
        }
        return findAllFriendsUser(id).stream()
                .filter(friend -> findAllFriendsUser(otherId).contains(friend))
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
        if (getUser(id).get().getFriends().containsKey(idFriends) &&
        getUser(idFriends).get().getFriends().containsKey(id)) {
            getUser(id).get().getFriends().put(idFriends, FriendshipStatus.CONFIRMED);
            getUser(idFriends).get().getFriends().put(id, FriendshipStatus.CONFIRMED);
        }
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
        if (newUser.getIdUser() == null) {
            log.warn("Поле id должно быть заполненно");
            throw new ValidationException("Id должен быть указан");
        }
        return userStorage.update(newUser);
    }
}

