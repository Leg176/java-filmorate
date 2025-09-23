package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Service
@Slf4j
public class RecommendationService {
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;

    public RecommendationService(FilmRepository filmRepository, UserRepository userRepository) {
        this.filmRepository = filmRepository;
        this.userRepository = userRepository;
    }

    public List<Film> getFilmRecommendations(Long userId) {
        log.info("Получение рекомендаций фильмов для пользователя {}", userId);

        // Получаем пользователя
        User user = userRepository.getUser(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Если у пользователя нет друзей - возвращаем пустой список
        if (user.getFriendship() == null || user.getFriendship().isEmpty()) {
            log.warn("У пользователя {} нет друзей", userId);
            return Collections.emptyList();
        }

        // Получаем фильмы, которые лайкал текущий пользователь
        Set<Long> userLikedFilms = new HashSet<>(filmRepository.findAllLikesFilm(userId));

        // Найдем лучшего "соседа" - друга с максимальным пересечением лайков
        Map<Long, Integer> similarityMap = new HashMap<>();
        for (Long friendId : user.getFriendship()) {
            if (friendId.equals(userId)) continue; // Пропускаем самого себя

            Set<Long> friendLikedFilms = new HashSet<>(filmRepository.findAllLikesFilm(friendId));

            // Рассчитываем пересечение лайков
            int commonLikes = 0;
            for (Long filmId : userLikedFilms) {
                if (friendLikedFilms.contains(filmId)) {
                    commonLikes++;
                }
            }

            similarityMap.put(friendId, commonLikes);
        }

        // Если нет друзей с общими лайками - возвращаем пустой список
        if (similarityMap.isEmpty()) {
            log.warn("Нет друзей с общими лайками для пользователя {}", userId);
            return Collections.emptyList();
        }

        // Находим друга с максимальным количеством общих лайков
        Long bestFriendId = Collections.max(similarityMap.entrySet(),
                Comparator.comparingInt(Map.Entry::getValue)).getKey();

        // Получаем фильмы, которые лайкал лучший друг
        Set<Long> bestFriendLikedFilms = new HashSet<>(filmRepository.findAllLikesFilms(bestFriendId));

        // Фильтруем фильмы, которые еще не лайкал текущий пользователь
        Set<Long> recommendedFilmIds = new HashSet<>(bestFriendLikedFilms);
        recommendedFilmIds.removeAll(userLikedFilms);

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
}
