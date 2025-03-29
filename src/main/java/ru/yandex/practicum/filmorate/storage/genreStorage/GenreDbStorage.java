package ru.yandex.practicum.filmorate.storage.genreStorage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;

import java.util.*;

@Repository
public class GenreDbStorage extends BaseDbStorage<Genre> {
    private static final String ADD_GENRE = "INSERT INTO genres (name) VALUES(?)";
    private static final String DELETE_GENRE = "DELETE FROM genres WHERE id = ?";
    private static final String GET_GENRE_BY_ID = "SELECT * FROM genres WHERE id = ?";
    private static final String GET_GENRES = "SELECT * FROM genres ORDER BY id";
    private static final String GET_FILMS_GENRES = "SELECT g.id, g.name FROM genres g" +
            " JOIN films_genres fg ON g.id = fg.genre_id WHERE fg.film_id = ? ORDER BY g.id";

    public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    public Genre addGenre(Genre genre) {
        return insertWithId(ADD_GENRE, GET_GENRE_BY_ID, genre.getName()).get();
    }

    public void deleteGenreById(Long id) {
        delete(DELETE_GENRE, id);
    }

    public Optional<Genre> getGenreById(Long id) {
        return findOne(GET_GENRE_BY_ID, id);
    }

    public List<Genre> getAllGenres() {
        return findMany(GET_GENRES);
    }

    public List<Genre> getFilmsGenres(Long id){
        return new ArrayList<>(findMany(GET_FILMS_GENRES, id));
    }

}
