package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.service.FilmService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Запрос на получение списка фильмов");
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable @Positive Long id) {
        log.info("Запрос на получение фильма по id");
        return filmService.getFilmById(id);
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        log.info("Запрос на добавление фильма");
        return filmService.createFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Запрос на обновление фильма");
        return filmService.updateFilm(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable @Positive Long id,
                        @PathVariable @Positive Long userId) {
        log.info("Запрос на добавление лайка");
        return filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLike(@PathVariable @Positive Long id,
                           @PathVariable @Positive Long userId) {
        log.info("Запрос на удаление лайка");
        return filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> showMostPopular(@RequestParam(defaultValue = "10") @Positive Long count) {
        log.info("Запрос на получение списка самых популярных фильмов");
        return filmService.showMostPopularFilms(count);
    }
}
