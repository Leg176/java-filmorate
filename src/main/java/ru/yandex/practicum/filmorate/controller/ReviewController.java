package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.review.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.review.ReviewDto;
import ru.yandex.practicum.filmorate.dto.review.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ReviewDto create(@RequestBody NewReviewRequest req) {
        return reviewService.create(req);
    }

    @PutMapping
    public ReviewDto update(@RequestBody UpdateReviewRequest req) {
        return reviewService.update(req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable @Positive Long id) {
        reviewService.delete(id);
    }

    @GetMapping("/{id}")
    public ReviewDto getById(@PathVariable @Positive Long id) {
        return reviewService.getById(id);
    }

    // ?filmId=&count=
    @GetMapping
    public List<ReviewDto> list(@RequestParam(value = "filmId", required = false) Long filmId,
                                @RequestParam(value = "count", required = false) Integer count) {
        return reviewService.getReviews(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public ReviewDto addLike(@PathVariable Long id, @PathVariable Long userId) {
        return reviewService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ReviewDto removeLike(@PathVariable Long id, @PathVariable Long userId) {
        return reviewService.removeLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public ReviewDto addDislike(@PathVariable Long id, @PathVariable Long userId) {
        return reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public ReviewDto removeDislike(@PathVariable Long id, @PathVariable Long userId) {
        return reviewService.removeDislike(id, userId);
    }
}
