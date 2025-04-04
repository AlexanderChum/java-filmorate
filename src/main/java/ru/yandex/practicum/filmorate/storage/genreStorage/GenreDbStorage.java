package ru.yandex.practicum.filmorate.storage.genreStorage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Repository
public class GenreDbStorage extends BaseDbStorage<Genre> {
    private static final String ADD_GENRE = "INSERT INTO genres (name) VALUES(:name)";
    private static final String GET_GENRE_BY_ID = "SELECT * FROM genres WHERE id = ?";
    private static final String GET_GENRES = "SELECT * FROM genres ORDER BY id";
    private static final String GET_FILMS_GENRES = "SELECT g.id, g.name FROM genres g" +
            " JOIN films_genres fg ON g.id = fg.genre_id WHERE fg.film_id = ? ORDER BY g.id";

    public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    public Genre addGenre(Genre genre) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", genre.getName());
        return insertWithId(ADD_GENRE, GET_GENRE_BY_ID, params)
                .orElseThrow(() -> new EntityNotFoundException("Не удалось добавить жанр"));
    }

    public Genre getOrCheckGenreById(Long id) {
        return findOne(GET_GENRE_BY_ID, id)
                .orElseThrow(() -> new EntityNotFoundException("Не удалось найти жанр"));
    }

    public List<Genre> getAllGenres() {
        return findMany(GET_GENRES);
    }

    public List<Genre> getFilmsGenres(Long id) {
        return new ArrayList<>(findMany(GET_FILMS_GENRES, id));
    }

}