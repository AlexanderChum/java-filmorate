package ru.yandex.practicum.filmorate.storage.filmStorage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    Film save(Film film);

    Film getOrCheckById(Long id);

    List<Film> getAll();

    void deleteFilmById(Long id);

    Film updateById(Long id, Film film);

    void addLike(Long filmId, Long userId);

    void deleteLike(Long filmId, Long userId);

    List<Long> getMostPopularFilms(Long limit);
}
