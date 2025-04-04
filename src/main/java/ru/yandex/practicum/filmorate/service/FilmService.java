package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.genreStorage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpaStorage.MPADbStorage;
import ru.yandex.practicum.filmorate.storage.filmStorage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.userStorage.UserStorage;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmDbStorage;
    private final UserStorage userDbStorage;
    private final MPADbStorage mpaDbStorage;
    private final GenreDbStorage genreDbStorage;

    public List<Film> getAllFilms() {
        log.info("Поступил запрос на получение списка фильмов");
        return filmDbStorage.getAll();
    }

    public Film getFilmById(Long id) {
        log.info("Поступил запрос на получение фильма по id");
        Film film = filmDbStorage.getOrCheckById(id);

        film.setGenres(genreDbStorage.getFilmsGenres(id));
        film.setMpa(mpaDbStorage.getOrCheckMpaById(film.getMpa().getId()));
        return film;
    }

    public Film createFilm(Film film) {
        log.info("Поступил запрос на добавление нового фильма");
        Film savedFilm = filmDbStorage.save(film);
        savedFilm.setMpa(mpaDbStorage.getOrCheckMpaById(film.getMpa().getId()));
        savedFilm.setGenres(genreDbStorage.getFilmsGenres(savedFilm.getId()));
        log.info("Фильм успешно сохранен");
        return savedFilm;
    }

    public Film updateFilm(Film newFilm) {
        log.info("Поступил запрос на обновление фильма");
        if (newFilm.getId() == null) {
            throw new ValidationException("Введен неверный id");
        }

        filmDbStorage.getOrCheckById(newFilm.getId());

        return filmDbStorage.updateById(newFilm.getId(), newFilm);
    }

    public Film addLike(Long id, Long userId) {
        log.info("Проверка пользователя на null");
        userDbStorage.getOrCheckById(userId);

        log.info("Проверка фильма на null");
        Film film = filmDbStorage.getOrCheckById(id);

        filmDbStorage.addLike(id, userId);
        log.info("Лайк от пользователя добавлен");
        return film;
    }

    public Film deleteLike(Long id, Long userId) {
        log.info("Проверка пользователя на null");
        userDbStorage.getOrCheckById(userId);

        log.info("Проверка фильма на null");
        Film film = filmDbStorage.getOrCheckById(id);

        filmDbStorage.deleteLike(id, userId);
        log.info("Лайк от пользователя удален");
        return film;
    }

    public List<Film> showMostPopularFilms(Long limit) {
        log.info("Поступил запрос на получение списка популярных фильмов");
        List<Film> resultList = new ArrayList<>();
        List<Long> filmIds = filmDbStorage.getMostPopularFilms(limit);
        for (Long id : filmIds) {
            resultList.add(filmDbStorage.getOrCheckById(id));
        }
        log.info("Список популярных фильмов создан");
        return resultList;
    }
}
