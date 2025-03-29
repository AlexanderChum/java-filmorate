package ru.yandex.practicum.filmorate.storage.filmStorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.GenreNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;
import ru.yandex.practicum.filmorate.storage.genreStorage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpaStorage.MPADbStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {
    private MPADbStorage mpaDbStorage;
    private GenreDbStorage genreDbStorage;

    private static final String GET_FILMS = "SELECT * FROM films";
    private static final String GET_FILM_BY_ID = "SELECT * FROM films WHERE id = ?";
    private static final String DELETE_FILM_BY_ID = "DELETE FROM films WHERE id = ?";
    private static final String INSERT_FILM = "INSERT INTO films (name, description, release_date, duration, mpa_id)" +
            " VALUES(?,?,?,?,?)";
    private static final String UPDATE_BY_ID = "UPDATE films SET name = ?, description = ?, release_date = ?," +
            " duration = ? WHERE id = ?";

    private static final String GET_MOST_POPULAR = "SELECT f.id FROM films f " +
            "LEFT JOIN likes l ON f.id = l.film_id GROUP BY f.id ORDER BY COUNT(l.user_id) DESC LIMIT ?";

    private static final String ADD_LIKE = "INSERT INTO likes (user_id, film_id) VALUES(?, ?)";
    private static final String DELETE_LIKE = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper,
                         MPADbStorage mpaDbStorage, GenreDbStorage genreDbStorage) {
        super(jdbc, mapper);
        this.mpaDbStorage = mpaDbStorage;
        this.genreDbStorage = genreDbStorage;
    }

    @Override
    public Film save(Film film) {
        log.info("Попытка сохранить фильм в базу данных");
        mpaDbStorage.getMpaById(film.getMpa().getId())
                .orElseThrow(() -> new MpaNotFoundException("Такого возрастного рейтинга не существует"));

        Film savedFilm = insertWithId(INSERT_FILM, GET_FILM_BY_ID,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId()).get();

        log.info("Обработка жанров для сохраняемого фильма");
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> uniqueIds = new HashSet<>(); //Отсортировываем дубликаты жанров (1,2,1)->(1,2)
            List<Genre> uniqueGenres = film.getGenres().stream()
                    .filter(g -> uniqueIds.add(g.getId()))
                    .collect(Collectors.toList());

            for (Genre genre : uniqueGenres) {
                genreDbStorage.getGenreById(genre.getId())
                        .orElseThrow(() -> new GenreNotFoundException("Такого жанра не существует"));
            }

            //привязка id фильма к его жанрам(id)
            String filmGenresQuery = "INSERT INTO films_genres (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : uniqueGenres) {
                jdbc.update(filmGenresQuery, savedFilm.getId(), genre.getId());
            }
        } else {
            film.setGenres(new ArrayList<>());
        }

        log.info("Попытка сохранить фильм успешна");
        return savedFilm;
    }

    @Override
    public Optional<Film> getById(Long id) {
        log.info("Попытка получить фильм из базы данных");
        return findOne(GET_FILM_BY_ID, id);
    }

    @Override
    public List<Film> getAll() {
        log.info("Попытка получить несколько фильмов из базы данных");
        return findMany(GET_FILMS);
    }

    @Override
    public void deleteFilmById(Long id) {
        log.info("Попытка удалить фильм из базы данных");
        delete(DELETE_FILM_BY_ID, id);
    }

    @Override
    public void updateById(Long id, Film film) {
        log.info("Попытка обновить фильм в базе данных");
        update(UPDATE_BY_ID,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                id);
    }

    public List<Long> getMostPopularFilms(Long limit) {
        log.info("Попытка получить список популярных фильмов из базы данных");
        return jdbc.queryForList(GET_MOST_POPULAR, Long.class, limit);
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Попытка добавить лайк в базу данных");
        insertWithoutCreatingId(ADD_LIKE, userId, filmId);
    }

    public void deleteLike(Long filmId, Long userId) {
        log.info("Попытка удалить лайк из базы данных");
        delete(DELETE_LIKE, filmId, userId);
    }
}
