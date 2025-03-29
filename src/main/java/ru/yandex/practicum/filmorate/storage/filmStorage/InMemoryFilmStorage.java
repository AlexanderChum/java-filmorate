package ru.yandex.practicum.filmorate.storage.filmStorage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film save(Film film) {
        if (film.getId() == null) {
            film.setId(getNextId());
        }
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Optional<Film> getById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public List<Film> getAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public void deleteFilmById(Long id) {
        films.remove(id);
    }

    @Override
    public void updateById(Long id, Film film) {
    }

    @Override
    public void addLike(Long id, Long userId) {
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
    }

    @Override
    public List<Long> getMostPopularFilms(Long limit) {
        return List.of();
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
