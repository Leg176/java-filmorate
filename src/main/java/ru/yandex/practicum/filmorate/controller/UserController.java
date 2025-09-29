package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.event.EventResponseDto;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.service.FeedService;
import ru.yandex.practicum.filmorate.service.RecommendationService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final FeedService feedService;
    private final RecommendationService recommendationService;

    @Autowired
    private UserController(UserService userService, FeedService feedService, RecommendationService recommendationService) {
        this.userService = userService;
        this.feedService = feedService;
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public Collection<UserDto> findAll() {
        return userService.getUsers();
    }

    @PostMapping
    public UserDto create(@Valid @RequestBody @NotNull NewUserRequest userRequest) {
        return userService.createUser(userRequest);
    }

    @PutMapping
    public UserDto update(@Valid @RequestBody @NotNull UpdateUserRequest request) {
        return userService.updateUser(request);
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable @Positive(message = "id должен быть больше 0") Long id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable @Positive(message = "id должен быть больше 0") Long id,
                          @PathVariable Long friendId) {
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable @Positive(message = "id должен быть больше 0") Long id,
                             @PathVariable @Positive(message = "friendId должен быть больше 0") Long friendId) {
        userService.deleteFriend(id, friendId);
    }

    @DeleteMapping("/{userId}")
    public void removeUser(@PathVariable @Positive(message = "id должен быть больше 0") Long userId) {
        userService.deleteUser(userId);
    }

    @GetMapping("/{id}/friends")
    public List<UserDto> findAllFriendsUser(@PathVariable @Positive(message = "id должен быть больше 0") Long id) {
        return userService.findAllFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<UserDto> findCommonFriends(@PathVariable @Positive(message = "id должен быть больше 0") Long id,
                                           @PathVariable @Positive(message = "otherId должен быть больше 0")
                                           Long otherId) {
        return userService.findCommonFriends(id, otherId);
    }

    @GetMapping("/{id}/feed")
    public List<EventResponseDto> getFeed(@PathVariable @Positive(message = "id должен быть больше 0") Long id) {
        return feedService.getFeed(id);
    }

    @GetMapping("/{id}/recommendations")
    public List<FilmDto> getRecommendations(@PathVariable Long id) {
        return recommendationService.getRecommendations(id);
    }
}
