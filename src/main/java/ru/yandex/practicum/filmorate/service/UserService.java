package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.EventType;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final EventService eventService;

    @Autowired
    public UserService(UserRepository userRepository, EventService eventService) {
        this.userRepository = userRepository;
        this.eventService = eventService;
    }

    public UserDto createUser(NewUserRequest request) {
        validationRequest(request);
        User user = UserMapper.mapToUser(request);
        user = userRepository.save(user);
        return UserMapper.mapToUserDto(user);
    }

    public List<UserDto> findAllFriends(Long userId) {
        validationUserIsEmpty(userId);
        return userRepository.findAllFriends(userId).stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long userId) {
        User user = userRepository.getUser(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        List<Long> friends = userRepository.findAllFriends(userId);
        user.setFriendship(new HashSet<>(friends));
        return UserMapper.mapToUserDto(user);
    }

    public List<UserDto> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public UserDto updateUser(UpdateUserRequest request) {
        validationUserIsEmpty(request.getId());

        boolean isLogin = userRepository.findAll().stream()
                .filter(user -> !user.getId().equals(request.getId()))
                .map(User::getLogin)
                .anyMatch(login -> login.equals(request.getLogin()));
        if (isLogin) {
            throw new ValidationException("Пользователь с Login: " + request.getLogin() + "существует");
        }
        User updatedUser = UserMapper.updateUserFields(userRepository.getUser(request.getId()).get(), request);
        userRepository.update(updatedUser);
        return UserMapper.mapToUserDto(updatedUser);
    }

    public List<UserDto> findCommonFriends(Long id, Long otherId) {
        validationUserIsEmpty(id);
        validationUserIsEmpty(otherId);
        return userRepository.findJointFriendsUsers(id, otherId).stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public void addFriend(Long userId, Long friendId) {
        validationIdFriends(userId, friendId);
        validationUserIsEmpty(userId);
        validationUserIsEmpty(friendId);
        if (userRepository.existsFriendship(userId, friendId)) {
            throw new ValidationException("Пользователи уже являются друзьями");
        }
        userRepository.addFriend(userId, friendId);
        eventService.add(friendId, userId, EventType.FRIEND);
    }

    public void deleteFriend(Long userId, Long friendId) {
        validationIdFriends(userId, friendId);
        validationUserIsEmpty(userId);
        validationUserIsEmpty(friendId);
        userRepository.deleteFriends(userId, friendId);
        eventService.delete(friendId, userId, EventType.FRIEND);

    }

    public void deleteUser(Long userId) {
        validationUserIsEmpty(userId);
        userRepository.deleteUser(userId);
    }

    private void validationIdFriends(Long id1, Long id2) {
        if (id1.equals(id2)) {
            throw new ValidationException("Нельзя удалить/добавить самого себя из друзей");
        }
    }

    public void validationUserIsEmpty(Long id) {
        if (userRepository.getUser(id).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + id + " в списках зарегестрированных не найден");
        }
    }

    private void validationRequest(NewUserRequest request) {
        Optional<User> alreadyExistUser = userRepository.findByEmail(request.getEmail());
        if (alreadyExistUser.isPresent()) {
            throw new ValidationException("Пользователь с таким имейл уже существует");
        }
    }
}
