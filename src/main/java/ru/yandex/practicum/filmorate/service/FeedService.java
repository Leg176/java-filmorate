package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FeedRepository;
import ru.yandex.practicum.filmorate.dto.event.EventResponseDto;
import ru.yandex.practicum.filmorate.mapper.EventMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedService {
    private final FeedRepository feedRepository;

    public FeedService(FeedRepository feedRepository) {
        this.feedRepository = feedRepository;
    }

    public List<EventResponseDto> getFeed(Long id) {
        return feedRepository.getFeedForUser(id).stream().map(EventMapper::toDto).collect(Collectors.toList());
    }

}
