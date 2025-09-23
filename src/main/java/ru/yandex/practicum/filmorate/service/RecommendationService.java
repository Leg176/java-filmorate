package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RecommendationService {
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    private final FilmMapper filmMapper;

    public RecommendationService(FilmRepository filmRepository, UserRepository userRepository, FilmMapper filmMapper) {
        this.filmRepository = filmRepository;
        this.userRepository = userRepository;
        this.filmMapper = filmMapper;
    }

    public List<FilmDto> getRecommendations(Long userId) {
        User user = userRepository.getUser(userId).orElseThrow(() -> new RuntimeException("User not found"));
        List<Long> likedFilms = filmRepository.findAllLikesFilm(userId);

        // Найти пользователей с максимальным количеством пересечения по лайкам
        Map<Long, Integer> similarUsers = new HashMap<>();
        for (Long friendId : user.getFriendship()) {
            List<Long> friendLikedFilms = filmRepository.findAllLikesFilm(friendId);
            int intersection = 0;
            for (Long filmId : likedFilms) {
                if (friendLikedFilms.contains(filmId)) {
                    intersection++;
                }
            }
            similarUsers.put(friendId, intersection);
        }

        // Определить фильмы, которые один пролайкал, а другой нет
        Set<Long> recommendedFilms = new HashSet<>();
        similarUsers.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    List<Long> friendLikedFilms = filmRepository.findAllLikesFilm(entry.getKey());
                    for (Long filmId : friendLikedFilms) {
                        if (!likedFilms.contains(filmId)) {
                            recommendedFilms.add(filmId);
                        }
                    }
                });

        // Рекомендовать фильмы, которым поставил лайк пользователь с похожими вкусами
        List<Film> films = filmRepository.findMany("SELECT * FROM Films WHERE idFilm IN (" +
                String.join(",", recommendedFilms.stream().map(String::valueOf).collect(Collectors.toList())) + ")");

        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }
}
