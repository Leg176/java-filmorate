package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FeedRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dto.event.EventResponseDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.EventMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedService {
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;

    public FeedService(FeedRepository feedRepository, UserRepository userRepository) {
        this.feedRepository = feedRepository;
        this.userRepository = userRepository;
    }

    public List<EventResponseDto> getFeed(Long id) {
        userRepository.getUser(id).orElseThrow(() -> new NotFoundException("Пользователя с id: " + " не существует"));
        return feedRepository.getFeedForUser(id).stream().map(EventMapper::toDto).collect(Collectors.toList());
    }

}
