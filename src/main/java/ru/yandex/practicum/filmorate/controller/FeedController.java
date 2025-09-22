package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.feed.FeedEventDto;
import ru.yandex.practicum.filmorate.service.FeedService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping("/{id}/feed")
    public List<FeedEventDto> getUserFeed(@PathVariable("id") @Positive Long userId,
                                          @RequestParam(value = "scope", required = false, defaultValue = "own_and_friends")
                                          String scope) {
        FeedService.Scope s = switch (scope.toLowerCase()) {
            case "own"     -> FeedService.Scope.OWN;
            case "friends" -> FeedService.Scope.FRIENDS;
            default        -> FeedService.Scope.OWN_AND_FRIENDS;
        };
        return feedService.getUserFeed(userId, s.name());
    }
}