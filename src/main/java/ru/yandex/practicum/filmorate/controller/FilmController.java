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
        log.trace("Запрос на получение списка фильмов принят");
        return filmService.getAllFilms();
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        log.trace("Запрос на добавление фильма принят");
        return filmService.createFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.trace("Запрос на обновление фильма принят");
        return filmService.updateFilm(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable @Positive Long id,
                        @PathVariable @Positive Long userId) {
        log.trace("Запрос на добавление лайка принят");
        return filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLike(@PathVariable @Positive Long id,
                           @PathVariable @Positive Long userId) {
        log.trace("Запрос на удаление лайка принят");
        return filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> showMostPopular(@RequestParam(defaultValue = "10") @Positive Long count) {
        log.trace("Запрос на получение списка самых популярных фильмов принят");
        return filmService.showMostPopularFilms(count);
    }
}
