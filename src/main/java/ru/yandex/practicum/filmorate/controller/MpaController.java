package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;
import ru.yandex.practicum.filmorate.serviceBD.MpaServiceBD;

import java.util.Collection;

@RestController
@RequestMapping("/mpa")
public class MpaController {

    private final MpaServiceBD mpaServiceBD;

    @Autowired
    public MpaController(MpaServiceBD mpaServiceBD) {
        this.mpaServiceBD = mpaServiceBD;
    }

    @GetMapping
    public Collection<MotionPictureAssociation> findAll() {
        return mpaServiceBD.getAllMpa();
    }

    @GetMapping("/{id}")
    public MotionPictureAssociation getMpa(@PathVariable Long id) {
        return mpaServiceBD.getMpa(id);
    }
}
