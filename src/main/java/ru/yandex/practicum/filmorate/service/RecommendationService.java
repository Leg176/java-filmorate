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
        User user = userRepository.getUser(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Получаем фильмы, которые уже лайкал пользователь
        List<Long> likedFilms = filmRepository.findAllLikesFilm(userId);

        if (likedFilms.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Integer> similarUsers = new HashMap<>();

        for (Long friendId : user.getFriendship()) {
            List<Long> friendLikedFilms = filmRepository.findAllLikesFilm(friendId);
            int commonCount = 0;

            for (Long filmId : likedFilms) {
                if (friendLikedFilms.contains(filmId)) {
                    commonCount++;
                }
            }

            similarUsers.put(friendId, commonCount);
        }

        if (similarUsers.isEmpty()) {
            return Collections.emptyList();
        }

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

        if (recommendedFilms.isEmpty()) {
            return Collections.emptyList();
        }

        String ids = String.join(",", recommendedFilms.stream().map(String::valueOf).collect(Collectors.toList()));
        String sql = "SELECT * FROM Films WHERE idFilm IN (" + ids + ") ORDER BY likes DESC LIMIT 10";

        List<Film> films = filmRepository.findMany(sql);

        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }


}
