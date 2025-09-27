package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.dto.director.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.director.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DirectorService {

    private final DirectorRepository directorRepository;

    @Autowired
    public DirectorService(DirectorRepository directorRepository) {
        this.directorRepository = directorRepository;
    }

    public Director createDirector(NewDirectorRequest request) {
        validationRequest(request);
        return directorRepository.save(DirectorMapper.mapToDirector(request));
    }

    public Director getDirectorById(Long idDirector) {
        return directorRepository.findById(idDirector)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id = " + idDirector + " не найден"));
    }

    public List<Director> getAllDirectors() {
        return directorRepository.getAllDirector();
    }

    public void deleteDirector(Long idDirector) {
        directorRepository.deleteDirector(idDirector);
    }

    public Director updateDirector(UpdateDirectorRequest request) {
        validationDirectorIsEmpty(request.getId());

        boolean isLogin = directorRepository.getAllDirector().stream()
                .filter(director -> !director.getId().equals(request.getId()))
                .map(Director::getName)
                .anyMatch(firstName -> firstName.equals(request.getName()));
        if (isLogin) {
            throw new ValidationException("Режиссёр с именем: " + request.getName() + "существует");
        }
        Director updatedDirector = DirectorMapper.updateDirectorFields(directorRepository.findById(request.getId())
                .get(), request);
        directorRepository.update(updatedDirector);
        return updatedDirector;
    }

    private void validationRequest(NewDirectorRequest request) {
        Optional<Director> alreadyExistUser = directorRepository.findByFirstName(request.getName());
        if (alreadyExistUser.isPresent()) {
            throw new ValidationException("Режиссёр с таким именем уже существует");
        }
    }

    public void validationDirectorIsEmpty(Long id) {
        if (directorRepository.findById(id).isEmpty()) {
            throw new NotFoundException("Режиссёр с id = " + id + " в списках зарегестрированных не найден");
        }
    }
}
