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
        if (request == null) {
            throw new ValidationException("Запрос на добавление нового пользователя не может быть пустым");
        }
        Optional<User> alreadyExistUser = userRepository.findByEmail(request.getEmail());
        if (alreadyExistUser.isPresent()) {
            throw new ValidationException("Пользователь с таким имейл уже существует");
        }
        User user = UserMapper.mapToUser(request);
        User userWithId = userRepository.save(user);
        return UserMapper.mapToUserDto(userWithId);
    }

    public List<UserDto> findAllFriends(long userId) {
        if (userId <= 0) {
            throw new ValidationException("id не может быть отрицательными или равными 0");
        }
        return userRepository.findAllFriends(userId).stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(long userId) {
        if (userId <= 0) {
            throw new ValidationException("id не может быть отрицательными или равными 0");
        }
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
        User updatedUser = userRepository.getUser(request.getIdUser())
                .map(user -> UserMapper.updateUserFields(user, request))
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + request.getIdUser() + " не найден"));
        userRepository.update(updatedUser);
        return UserMapper.mapToUserDto(updatedUser);
    }

    public List<UserDto> findJoinFriendsUsers(long id, long otherId) {
        if (id <= 0 || otherId <= 0) {
            throw new ValidationException("id не может быть отрицательным или равным 0");
        }
        validationUserIsEmpty(id);
        validationUserIsEmpty(otherId);
        return userRepository.findJointFriendsUsers(id, otherId).stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public void addFriend(long userId, long friendId) {
        validationIdFriends(userId, friendId);
        validationUserIsEmpty(userId);
        validationUserIsEmpty(friendId);
        if (userRepository.findAllFriends(userId).contains(friendId)) {
            throw new ValidationException("Пользователи уже являются друзьями");
        }
        userRepository.addFriend(userId, friendId);
    }

    public void deleteFriend(long userId, long friendId) {
        validationIdFriends(userId, friendId);
        validationUserIsEmpty(userId);
        validationUserIsEmpty(friendId);
        if (!userRepository.findAllFriends(userId).contains(friendId)) {
            throw new ValidationException("Пользователи не являются друзьями");
        }
        userRepository.deleteFriends(userId, friendId);
    }

    private void validationIdFriends(long id1, long id2) {
        if (id1 <= 0 || id2 <= 0) {
            throw new ValidationException("id не могут быть отрицательными или равными 0");
        }
        if (id1 == id2) {
            throw new ValidationException("Нельзя удалить/добавить самого себя из друзей");
        }
    }

    private void validationUserIsEmpty(long id) {
        if (userRepository.getUser(id).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + id + " в списках зарегестрированных не найден");
        }
    }
}
