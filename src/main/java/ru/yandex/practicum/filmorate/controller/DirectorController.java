package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.director.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.director.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@RestController
@RequestMapping("/directors")
public class DirectorController {

    private final DirectorService directorService;

    @Autowired
    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }

    @GetMapping
    public Collection<Director> findAll() {
        return directorService.getAllDirectors();
    }

    @GetMapping("/{id}")
    public Director getDirector(@PathVariable @Positive(message = "id должен быть больше 0") Long id) {
        return directorService.getDirectorById(id);
    }

    @PostMapping
    public Director create(@Valid @RequestBody NewDirectorRequest request) {
        return directorService.createDirector(request);
    }

    @PutMapping
    public Director update(@Valid @RequestBody UpdateDirectorRequest request) {
        return directorService.updateDirector(request);
    }

    @DeleteMapping("/{id}")
    public void delDirector(@PathVariable @Positive(message = "id должен быть больше 0") Long id) {
        directorService.deleteDirector(id);
    }
}
