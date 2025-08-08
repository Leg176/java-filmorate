package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserStorage userStorage;
    private final UserService userService;

    @Autowired
    private UserController(UserStorage userStorage, UserService userService){
        this.userStorage = userStorage;
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        return userStorage.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        return userStorage.update(newUser);
    }

    @GetMapping("/{id}")
    public Optional<User> getUser(@PathVariable Long id) {
        return userStorage.getUser(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        if (userStorage.getUser(id).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + id + " в списках зарегестрированных не найден");
        }
        if (userStorage.getUser(friendId).isEmpty()) {
            throw new NotFoundException("Друг с id = " + friendId + " в списках зарегестрированных не найден");
        }
        if (id == friendId) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }
        userService.addFriend(userStorage.getUser(id).get(), userStorage.getUser(friendId).get());
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable @Min(1) Long id, @PathVariable @Min(1) Long friendId) {
        if (userStorage.getUser(id).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + id + " в списках зарегестрированных не найден");
        }
        if (userStorage.getUser(friendId).isEmpty()) {
            throw new NotFoundException("Друг с id = " + friendId + " в списках зарегестрированных не найден");
        }
        if (id == friendId) {
            throw new NotFoundException("Нельзя удалить самого себя из друзей");
        }
        userService.removeFriend(userStorage.getUser(id).get(), userStorage.getUser(friendId).get());
    }

    @GetMapping("/{id}/friends")
    public List<User> findAllFriendsUser(@PathVariable @Min(1) Long id) {
        if (userStorage.getUser(id).isPresent()) {
            return userService.findAllFriendsUser(userStorage.getUser(id).get());
        } else {
            throw new NotFoundException("Пользователь с id = " + id + " в списках зарегестрированных не найден");
        }
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> findСommonFriendsUsers(@PathVariable @Min(1) Long id, @PathVariable @Min(1) Long otherId) {
        if (userStorage.getUser(id).isPresent()) {
            if (userStorage.getUser(otherId).isPresent()) {
                return userService.findСommonFriendsUsers
                        (userStorage.getUser(id).get(), userStorage.getUser(otherId).get());
            } else {
                throw new NotFoundException("Пользователь с id = " + otherId +
                        " в списках зарегестрированных не найден");
            }
        } else {
            throw new NotFoundException("Пользователь с id = " + id +
                    " в списках зарегестрированных не найден");
        }
    }
}
