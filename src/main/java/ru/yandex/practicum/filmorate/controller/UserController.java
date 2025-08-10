package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    private UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        return userService.findAll();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        return userService.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        return userService.update(newUser);
    }

    @GetMapping("/{id}")
    public Optional<User> getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        if (userService.getUser(id).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + id + " в списках зарегестрированных не найден");
        }
        if (userService.getUser(friendId).isEmpty()) {
            throw new NotFoundException("Друг с id = " + friendId + " в списках зарегестрированных не найден");
        }
        if (id == friendId) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }
        userService.addFriend(userService.getUser(id).get(), userService.getUser(friendId).get());
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable @Min(1) Long id, @PathVariable @Min(1) Long friendId) {
        if (userService.getUser(id).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + id + " в списках зарегестрированных не найден");
        }
        if (userService.getUser(friendId).isEmpty()) {
            throw new NotFoundException("Друг с id = " + friendId + " в списках зарегестрированных не найден");
        }
        if (id == friendId) {
            throw new NotFoundException("Нельзя удалить самого себя из друзей");
        }
        userService.removeFriend(userService.getUser(id).get(), userService.getUser(friendId).get());
    }

    @GetMapping("/{id}/friends")
    public List<User> findAllFriendsUser(@PathVariable @Min(1) Long id) {
        if (userService.getUser(id).isPresent()) {
            return userService.findAllFriendsUser(userService.getUser(id).get());
        } else {
            throw new NotFoundException("Пользователь с id = " + id + " в списках зарегестрированных не найден");
        }
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> findСommonFriendsUsers(@PathVariable @Min(1) Long id, @PathVariable @Min(1) Long otherId) {
        if (userService.getUser(id).isPresent()) {
            if (userService.getUser(otherId).isPresent()) {
                return userService.findСommonFriendsUsers(userService.getUser(id).get(),
                        userService.getUser(otherId).get());
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
