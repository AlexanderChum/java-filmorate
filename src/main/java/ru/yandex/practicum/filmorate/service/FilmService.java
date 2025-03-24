package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public List<Film> getAllFilms() {
        log.info("Поступил запрос на получение списка фильмов");
        return filmStorage.getAll();
    }

    public Film createFilm(Film film) {
        log.info("Поступил запрос на добавление нового фильма");
        filmStorage.save(film);
        log.info("Добавление нового фильма завершено");
        return film;
    }

    public Film updateFilm(Film newFilm) {
        log.info("Поступил запрос на обновление фильма");
        if (newFilm.getId() == null) {
            throw new ValidationException("Введен неверный id");
        }

        filmExistence(newFilm.getId());

        filmStorage.save(newFilm);
        log.info("Фильм с нужным id найден и обновлен");
        return newFilm;
    }

    public Film addLike(Long id, Long userId) {
        log.info("Проверка пользователя на null");
        userStorage.getById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователя с указанным id не найдено"));

        log.info("Проверка фильма на null");
        Film film = filmExistence(id);

        film.getLikeSet().add(userId);
        log.info("Лайк от пользователя добавлен");
        return film;
    }

    public Film deleteLike(Long id, Long userId) {
        log.info("Проверка пользователя на null");
        userStorage.getById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователя с указанным id не найдено"));

        log.info("Проверка фильма на null");
        Film film = filmExistence(id);

        film.getLikeSet().remove(userId);
        log.info("Лайк от пользователя удален");
        return film;
    }

    public List<Film> showMostPopularFilms(Long count) {
        log.info("Поступил запрос на получение списка популярных фильмов");
        return filmStorage.getAll().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikeSet().size()).reversed())
                .limit(Math.min(count, filmStorage.getAll().size()))
                .collect(Collectors.toList());
    }

    private Film filmExistence(Long id) {
        return filmStorage.getById(id)
                .orElseThrow(() -> new FilmNotFoundException("Фильма с id = " + id + " не найдено"));
    }
}
