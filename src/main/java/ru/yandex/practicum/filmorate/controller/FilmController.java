package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validators.FilmValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private static final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public List<Film> getAll() {
        log.info("Список фильмов возвращен");
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Поступил запрос на добавление нового фильма");
        FilmValidator.filmValidation(film);
        log.info("Запрос прошел валидацию и отправлен на добавление");
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Добавление нового фильма завершено");
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        log.info("Поступил запрос на обновление фильма");
        FilmValidator.filmValidation(film);
        log.info("Запрос прошел валидацию");
        if (film.getId() == null) {
            throw new ValidationException("Введен неверный id");
        }
        if (!films.containsKey(film.getId())) {
            throw new ValidationException("Фильма с указанным id не найдено");
        }
        films.put(film.getId(), film);
        log.info("Фильм с нужным id найден и обновлен");
        return film;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
