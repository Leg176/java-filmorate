package ru.yandex.practicum.filmorate.serviceBD;

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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceBD {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceBD (UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto createUser(NewUserRequest request) {
        validationRequest(request);
        User user = UserMapper.mapToUser(request);
        user = userRepository.save(user);
        return UserMapper.mapToUserDto(user);
    }

    public List<UserDto> findAllFriends(Long userId) {
        validationId(userId);
        validationUserIsEmpty(userId);
        return userRepository.findAllFriends(userId).stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long userId) {
        validationId(userId);
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
        if (request == null) {
            throw new ValidationException("Запрос на обновление данных пользователя не может быть пустым");
        }
        validationUserIsEmpty(request.getId());

        boolean isLogin = userRepository.findAll().stream()
                .filter(user -> user.getIdUser().equals(request.getId()))
                .map(User::getLogin)
                .anyMatch(login -> login.equals(request.getLogin()));
        if (isLogin) {
            throw new ValidationException("Пользователь с Login: " + request.getLogin() + "существует");
        }
        User updatedUser = UserMapper.updateUserFields(userRepository.getUser(request.getId()).get(), request);
        userRepository.update(updatedUser);
        return UserMapper.mapToUserDto(updatedUser);
    }

    public List<UserDto> findJoinFriendsUsers(Long id, Long otherId) {
        validationId(id);
        validationId(otherId);
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
        if (userRepository.findAllFriends(userId).contains(friendId)) {
            throw new ValidationException("Пользователи уже являются друзьями");
        }
        userRepository.addFriend(userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        validationIdFriends(userId, friendId);
        validationUserIsEmpty(userId);
        validationUserIsEmpty(friendId);
        userRepository.deleteFriends(userId, friendId);
    }

    private void validationIdFriends(Long id1, Long id2) {
        validationId(id1);
        validationId(id2);
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
        if (request == null) {
            throw new ValidationException("Запрос на добавление нового пользователя не может быть пустым");
        }
        Optional<User> alreadyExistUser = userRepository.findByEmail(request.getEmail());
        if (alreadyExistUser.isPresent()) {
            throw new ValidationException("Пользователь с таким имейл уже существует");
        }
    }

    private void validationId(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("id не может быть отрицательным, равными 0 или null");
        }
    }
}
