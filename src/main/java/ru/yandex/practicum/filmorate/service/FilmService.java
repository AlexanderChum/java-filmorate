package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film createFilm(Film film) {
        log.info("Поступил запрос на добавление нового фильма");
        filmStorage.save(film);
        log.info("Добавление нового фильма завершено");
        return film;
    }

    public Film updateFilm(Film film) {
        log.info("Поступил запрос на обновление фильма");
        if (film.getId() == null) {
            throw new ValidationException("Введен неверный id");
        }
        if (filmStorage.getById(film.getId()).isEmpty()) {
            throw new FilmNotFoundException("Фильма с указанным id не найдено");
        }
        filmStorage.save(film);
        log.info("Фильм с нужным id найден и обновлен");
        return film;
    }

    public Film addLike(Long id, Long userId) {
        Optional<User> userOpt = userStorage.getById(userId);
        log.trace("Проверка пользователя на null");
        if (userOpt.isEmpty()) throw new UserNotFoundException("Пользователя с указанным id не найдено");

        Optional<Film> filmOpt = filmStorage.getById(id);
        log.trace("Проверка фильма на null");
        if (filmOpt.isEmpty()) throw new FilmNotFoundException("Фильма с указанным id не найдено");

        filmOpt.get().getLikeSet().add(userId);
        log.info("Лайк от пользователя добавлен");
        return filmOpt.get();
    }

    public Film deleteLike(Long id, Long userId) {
        Optional<User> userOpt = userStorage.getById(userId);
        log.trace("Проверка пользователя на null");
        if (userOpt.isEmpty()) throw new UserNotFoundException("Пользователя с указанным id не найдено");

        Optional<Film> filmOpt = filmStorage.getById(id);
        log.trace("Проверка фильма на null");
        if (filmOpt.isEmpty()) throw new FilmNotFoundException("Фильма с указанным id не найдено");

        filmOpt.get().getLikeSet().remove(userId);
        log.info("Лайк от пользователя удален");
        return filmOpt.get();
    }

    public List<Film> showMostPopularFilms(Long count) {
        return filmStorage.getAll().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikeSet().size()).reversed())
                .limit(Math.min(count, filmStorage.getAll().size()))
                .collect(Collectors.toList());
    }
}
