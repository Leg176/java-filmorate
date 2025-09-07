package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.serviceBD.UserServiceBD;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserServiceBD userServiceBD;

    @Autowired
    private UserController(UserServiceBD userServiceBD) {
        this.userServiceBD = userServiceBD;
    }

    @GetMapping
    public Collection<UserDto> findAll() {
        return userServiceBD.getUsers();
    }

    @PostMapping
    public UserDto create(@Valid @RequestBody NewUserRequest userRequest) {
        return userServiceBD.createUser(userRequest);
    }

    @PutMapping
    public UserDto update(@Valid @RequestBody UpdateUserRequest request) {
        return userServiceBD.updateUser(request);
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable Long id) {
        return userServiceBD.getUserById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userServiceBD.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable @Min(1) Long id, @PathVariable @Min(1) Long friendId) {
        userServiceBD.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<UserDto> findAllFriendsUser(@PathVariable @Min(1) Long id) {
        return userServiceBD.findAllFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<UserDto> findJoinFriendsUsers(@PathVariable @Min(1) Long id, @PathVariable @Min(1) Long otherId) {
        return userServiceBD.findJoinFriendsUsers(id, otherId);

    }
}
