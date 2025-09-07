package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {

    public static User mapToUser(NewUserRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request не может быть пустым!");
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setLogin(request.getLogin());
        user.setName(request.getName());
        user.setBirthday(request.getBirthday());
        user.setFriendship(request.getFriendship());
        if(user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return user;
    }

    public static UserDto mapToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getIdUser());
        dto.setEmail(user.getEmail());
        dto.setLogin(user.getLogin());
        if(user.getName().isBlank()) {
            dto.setName(user.getLogin());
        } else {
            dto.setName(user.getName());
        }
        dto.setBirthday(user.getBirthday());
        dto.setFriendship(user.getFriendship());
        return dto;
    }

    public static User updateUserFields(User user, UpdateUserRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request не может быть пустым!");
        }

        if (request.hasEmail()) {
            user.setEmail(request.getEmail());
        }
        if (request.hasLogin()) {
            user.setLogin(request.getLogin());
        }

        if (request.hasName()) {
            user.setName(request.getName());
        }
        if (request.hasBirthday()) {
            user.setBirthday(request.getBirthday());
        }
        return user;
    }
}
