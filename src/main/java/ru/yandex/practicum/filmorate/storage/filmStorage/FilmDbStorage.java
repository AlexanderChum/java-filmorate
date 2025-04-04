package ru.yandex.practicum.filmorate.storage.filmStorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;
import ru.yandex.practicum.filmorate.storage.genreStorage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpaStorage.MPADbStorage;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Optional;

@Slf4j
@Repository
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {
    private final MPADbStorage mpaDbStorage;
    private final GenreDbStorage genreDbStorage;

    private static final String GET_FILMS = "SELECT * FROM films";
    private static final String GET_FILM_BY_ID = "SELECT * FROM films WHERE id = ?";
    private static final String DELETE_FILM_BY_ID = "DELETE FROM films WHERE id = ?";
    private static final String INSERT_FILM = "INSERT INTO films (name, description, release_date, duration, mpa_id)" +
            " VALUES(:name, :description, :releaseDate, :duration, :mpaId)";
    private static final String UPDATE_BY_ID = "UPDATE films SET name = :name, description = :description," +
            " release_date = :releaseDate, duration = :duration WHERE id = :id";

    private static final String GET_MOST_POPULAR = "SELECT f.id FROM films f " +
            "LEFT JOIN likes l ON f.id = l.film_id GROUP BY f.id ORDER BY COUNT(l.user_id) DESC LIMIT ?";

    private static final String ADD_LIKE = "INSERT INTO likes (user_id, film_id) VALUES(?, ?)";
    private static final String DELETE_LIKE = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String GET_LIKES_SET = "SELECT COUNT(*) FROM likes WHERE film_id = ?";

    private static final String GET_MPAID = "SELECT mpa_id FROM films WHERE id = ?";

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper,
                         MPADbStorage mpaDbStorage, GenreDbStorage genreDbStorage) {
        super(jdbc, mapper);
        this.mpaDbStorage = mpaDbStorage;
        this.genreDbStorage = genreDbStorage;
    }

    @Override
    public Film save(Film film) {
        log.info("Попытка сохранить фильм в базу данных");
        mpaDbStorage.getOrCheckMpaById(film.getMpa().getId());

        Map<String, Object> params = new HashMap<>();
        params.put("name", film.getName());
        params.put("description", film.getDescription());
        params.put("releaseDate", film.getReleaseDate());
        params.put("duration", film.getDuration());
        params.put("mpaId", film.getMpa().getId());

        Film savedFilm = insertWithId(INSERT_FILM, GET_FILM_BY_ID, params).get();

        log.info("Обработка жанров для сохраняемого фильма");
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> uniqueIds = new HashSet<>(); //Отсортировываем дубликаты жанров (1,2,1)->(1,2)
            List<Genre> uniqueGenres = film.getGenres().stream()
                    .filter(g -> uniqueIds.add(g.getId()))
                    .toList();

            for (Genre genre : uniqueGenres) {
                genreDbStorage.getOrCheckGenreById(genre.getId());
            }

            //привязка id фильма к его жанрам(id)
            String filmGenresQuery = "INSERT INTO films_genres (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : uniqueGenres) {
                jdbc.update(filmGenresQuery, savedFilm.getId(), genre.getId());
            }
        } else {
            film.setGenres(new ArrayList<>());
        }

        savedFilm.setMpa(mpaDbStorage.getOrCheckMpaById(film.getMpa().getId()));

        log.info("Попытка сохранить фильм успешна");
        return savedFilm;
    }

    @Override
    public Film getOrCheckById(Long id) {
        log.info("Попытка получить фильм из базы данных");
        Optional<Film> film = findOne(GET_FILM_BY_ID, id);

        if (film.isPresent()) {
            Long mpaId = jdbc.queryForObject(GET_MPAID, Long.class, film.get().getId());
            film.get().setMpa(mpaDbStorage.getOrCheckMpaById(mpaId));
            return film.get();
        } else {
            throw new EntityNotFoundException("Фильм с таким id не найден");
        }
    }

    @Override
    public List<Film> getAll() {
        log.info("Попытка получить несколько фильмов из базы данных");
        List<Film> resultWithMpa = findMany(GET_FILMS);

        for (Film film : resultWithMpa) {
            Long mpaId = jdbc.queryForObject(GET_MPAID, Long.class, film.getId());
            film.setMpa(mpaDbStorage.getOrCheckMpaById(mpaId));
        }

        return resultWithMpa;
    }

    @Override
    public void deleteFilmById(Long id) {
        log.info("Попытка удалить фильм из базы данных");
        delete(DELETE_FILM_BY_ID, id);
    }

    @Override
    public Film updateById(Long id, Film film) {
        log.info("Попытка обновить фильм в базе данных");
        Map<String, Object> params = new HashMap<>();
        params.put("name", film.getName());
        params.put("description", film.getDescription());
        params.put("releaseDate", film.getReleaseDate());
        params.put("duration", film.getDuration());
        params.put("id", id);

        update(UPDATE_BY_ID, params);
        return getOrCheckById(id);
    }

    public List<Long> getMostPopularFilms(Long limit) {
        log.info("Попытка получить список популярных фильмов из базы данных");
        return jdbc.queryForList(GET_MOST_POPULAR, Long.class, limit);
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Попытка добавить лайк в базу данных");
        jdbc.update(ADD_LIKE, userId, filmId);
    }

    public void deleteLike(Long filmId, Long userId) {
        log.info("Попытка удалить лайк из базы данных");
        delete(DELETE_LIKE, filmId, userId);
    }

    public List<Long> getLikes(Long filmId) {
        log.info("Попытка получения количества лайков");
        return new ArrayList<>(jdbc.queryForList(GET_LIKES_SET, Long.class, filmId));
    }
}