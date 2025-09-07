package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.serviceBD.GenreServiceBD;

import java.util.Collection;

@RestController
@RequestMapping("/genres")
public class GenresController {

    private final GenreServiceBD genreServiceBD;

    @Autowired
    public GenresController(GenreServiceBD genreServiceBD) {
        this.genreServiceBD = genreServiceBD;
    }

    @GetMapping
    public Collection<Genre> findAll() {
        return genreServiceBD.getAllGenre();
    }

    @GetMapping("/{id}")
    public Genre getGenre(@PathVariable Long id) {
        return genreServiceBD.getGenre(id);
    }
}
