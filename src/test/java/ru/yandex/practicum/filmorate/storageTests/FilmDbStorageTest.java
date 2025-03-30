package ru.yandex.practicum.filmorate.storageTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.filmStorage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genreStorage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.mpaStorage.MPADbStorage;
import ru.yandex.practicum.filmorate.storage.userStorage.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, MPADbStorage.class, GenreDbStorage.class,
        FilmRowMapper.class, GenreRowMapper.class, MpaRowMapper.class})
class FilmDbStorageTest {
    private final JdbcTemplate jdbc;
    private final FilmDbStorage filmStorage;
    private final MPADbStorage mpaStorage;
    private final GenreDbStorage genreStorage;

    private Film testFilm1 = testFilmCreation(null, "Film1", "New Desc", 110L);
    private Film testFilm2 = testFilmCreation(null, "Film2", "New Desc", 110L);
    private Film filmForUpdate = testFilmCreation(1L, "Updated", "Updated Desc", 110L);
    private MPA testMpa = createTestMpa(1L, "PG-13");

    private Genre testGenreDrama = createTestGenre(null, "Драма");
    private Genre testGenreComedy = createTestGenre(null, "Комедия");

    private User testUser = createTestUser(null);
    private User testUser2 = createTestUser(null);


    @BeforeEach
    void setup() {
        jdbc.update("DELETE FROM likes");
        jdbc.update("DELETE FROM films_genres");
        jdbc.update("DELETE FROM films");
        jdbc.update("DELETE FROM genres");
        jdbc.update("DELETE FROM mpa");
        jdbc.update("ALTER TABLE films ALTER COLUMN id RESTART WITH 1");
        jdbc.update("ALTER TABLE mpa ALTER COLUMN id RESTART WITH 1");
        jdbc.update("ALTER TABLE genres ALTER COLUMN id RESTART WITH 1");
        jdbc.update("INSERT INTO mpa (name) VALUES('PG-13')");
        testFilm1.setMpa(testMpa);
    }

    private static Film testFilmCreation(Long id, String name, String description, Long duration) {
        Film film = new Film();
        film.setId(id);
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(duration);
        film.setMpaId(1L);
        return film;
    }

    private MPA createTestMpa(Long id, String name) {
        MPA mpa = new MPA();
        mpa.setId(id);
        mpa.setName(name);
        return mpa;
    }

    private Genre createTestGenre(Long id, String name) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setName(name);
        return genre;
    }

    private User createTestUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail(id + "@ya.ru");
        user.setLogin("user" + id);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    //----------------------------------

    @Test
    void testSaveFilm() {
        mpaStorage.addMpa(testMpa);
        Film saved = filmStorage.save(testFilm1);

        assertThat(saved)
                .hasFieldOrPropertyWithValue("name", "Film1")
                .hasFieldOrPropertyWithValue("duration", 110L);
    }

    @Test
    void testGetFilmByIdWithRelations() {
        Film saved = filmStorage.save(testFilm1);
        Optional<Film> result = filmStorage.getById(saved.getId());

        assertEquals(saved.getName(), result.get().getName());
        assertEquals(saved.getDescription(), result.get().getDescription());
    }

    @Test
    void testUpdateFilm() {
        Film saved = filmStorage.save(testFilm1);

        filmStorage.updateById(saved.getId(), filmForUpdate);
        Optional<Film> result = filmStorage.getById(saved.getId());

        assertThat(result)
                .isPresent()
                .get()
                .hasFieldOrPropertyWithValue("name", "Updated")
                .hasFieldOrPropertyWithValue("description", "Updated Desc");
    }

    @Test
    void testDeleteFilm() {
        Film saved = filmStorage.save(testFilm1);
        filmStorage.deleteFilmById(saved.getId());

        assertThat(filmStorage.getById(saved.getId())).isEmpty();
    }

    @Test
    void testAddAndRemoveLike() {
        Film film = filmStorage.save(testFilm1);
        UserDbStorage userStorage = new UserDbStorage(jdbc, new UserRowMapper());
        User user = userStorage.save(testUser);

        filmStorage.addLike(film.getId(), user.getId());
        film.setLikeSet(filmStorage.getLikes(film.getId()));
        assertTrue(filmStorage.getLikes(film.getId()).contains(1L));

        filmStorage.deleteLike(film.getId(), user.getId());
        film.setLikeSet(filmStorage.getLikes(film.getId()));
        assertTrue(filmStorage.getLikes(film.getId()).contains(0L));
    }

    @Test
    void testGetMostPopularFilms() {
        Film film1 = filmStorage.save(testFilm1);
        testFilm2.setMpa(testMpa);
        Film film2 = filmStorage.save(testFilm2);

        UserDbStorage userStorage = new UserDbStorage(jdbc, new UserRowMapper());
        User user = userStorage.save(testUser);
        User user2 = userStorage.save(testUser2);


        filmStorage.addLike(film1.getId(), user.getId());
        filmStorage.addLike(film2.getId(), user.getId());
        filmStorage.addLike(film2.getId(), user2.getId());

        assertThat(filmStorage.getMostPopularFilms(2L))
                .containsExactly(film2.getId(), film1.getId());
    }

    @Test
    void testFilmGenreRelations() {
        Genre savedDrama = genreStorage.addGenre(testGenreDrama);
        Genre savedComedy = genreStorage.addGenre(testGenreComedy);

        testFilm1.setGenres(List.of(savedDrama, savedComedy));
        Film filmWithGenres = filmStorage.save(testFilm1);


        Film result = filmStorage.getById(filmWithGenres.getId()).get();
        result.setGenres(genreStorage.getFilmsGenres(result.getId()));
        assertThat(result)
                .extracting(Film::getGenres)
                .asList()
                .containsExactlyInAnyOrder(savedDrama, savedComedy);
    }
}
