package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/genres")
public class GenreController {
    private final GenreService genreService;

    @GetMapping
    public List<Genre> getAllGenres() {
        log.info("Запрос на получение всех жанров");
        return genreService.getAllGenres();
    }

    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable @Positive Long id) {
        log.info("Запрос на получение жанра по id");
        return genreService.getGenreById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteGenreById(@PathVariable @Positive Long id) {
        log.info("Запрос на удаление жанра по id");
        genreService.deleteGenreById(id);
    }

    @PostMapping
    public Genre addGenre(@Valid @RequestBody Genre genre) {
        log.info("Запрос на добавление нового жанра");
        return genreService.addGenre(genre);
    }
}
