package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;

    public List<Film> getRecommendations(Long userId) {
        log.info("Получение рекомендаций для пользователя с ID {}", userId);

        // Получаем пользователя и его лайки
        User user = userRepository.getUser(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        List<Long> userLikes = getUserLikedFilms(user.getIdUser());

        // Находим пользователей с максимальным количеством пересечения по лайкам
        Map<Long, Integer> similarUsers = findSimilarUsers(userLikes);

        if (similarUsers.isEmpty()) {
            log.warn("Не найдены пользователи с похожими предпочтениями для пользователя {}", userId);
            return Collections.emptyList();
        }

        // Определяем фильмы, которые понравились похожим пользователям,
        // но ещё не понравились текущему пользователю
        Set<Long> recommendedFilmIds = new HashSet<>();

        for (Map.Entry<Long, Integer> entry : similarUsers.entrySet()) {
            List<Long> similarUserLikes = getUserLikedFilms(entry.getKey());

            for (Long filmId : similarUserLikes) {
                if (!userLikes.contains(filmId)) {
                    recommendedFilmIds.add(filmId);
                }
            }
        }

        // Получаем информацию о рекомендуемых фильмах
        List<Film> recommendedFilms = new ArrayList<>();
        for (Long filmId : recommendedFilmIds) {
            Film film = filmRepository.getFilm(filmId)
                    .orElseThrow(() -> new RuntimeException("Фильм не найден"));
            recommendedFilms.add(film);
        }

        log.info("Для пользователя {} найдено {} рекомендаций", userId, recommendedFilms.size());
        return recommendedFilms;
    }

    private List<Long> getUserLikedFilms(Long userId) {
        return filmRepository.findAllLikesFilm(userId);
    }

    private Map<Long, Integer> findSimilarUsers(List<Long> userLikes) {
        Map<Long, Integer> similarUsers = new HashMap<>();

        for (Long filmId : userLikes) {
            List<Long> usersWhoLiked = filmRepository.findAllUsersWhoLiked(filmId);

            for (Long otherUserId : usersWhoLiked) {
                if (!otherUserId.equals(userLikes.iterator().next())) {  // исключаем самого пользователя
                    similarUsers.put(otherUserId, similarUsers.getOrDefault(otherUserId, 0) + 1);
                }
            }
        }

        // Сортируем пользователей по количеству совпадений (в убывающем порядке)
        return similarUsers.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));
    }
}
