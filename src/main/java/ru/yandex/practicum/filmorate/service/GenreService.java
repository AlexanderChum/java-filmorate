package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genreStorage.GenreDbStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreDbStorage genreDbStorage;

    public List<Genre> getAllGenres() {
        log.info("Поступил запрос на получение списка всех жанров");
        return genreDbStorage.getAllGenres();
    }

    public Genre getGenreById(Long id) {
        log.info("Поступил запрос на получение жанра по id");
        return genreDbStorage.getGenreById(id)
                .orElseThrow(() -> new EntityNotFoundException("Жанр не найден"));
    }

    public Genre addGenre(Genre genre) {
        log.info("Поступил запрос на добавление жанра");
        return genreDbStorage.addGenre(genre);
    }
}
