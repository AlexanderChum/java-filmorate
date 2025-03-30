package ru.yandex.practicum.filmorate.serviceTests;

import static org.junit.jupiter.api.Assertions.*;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.filmStorage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genreStorage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.mpaStorage.MPADbStorage;
import ru.yandex.practicum.filmorate.storage.userStorage.UserDbStorage;

import java.time.LocalDate;
import java.util.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, MPADbStorage.class, GenreDbStorage.class,
        FilmRowMapper.class, GenreRowMapper.class, MpaRowMapper.class})
class FilmServiceTests {
    @Autowired
    private JdbcTemplate jdbc;
    private FilmDbStorage filmStorage;
    private UserDbStorage userStorage;
    private MPADbStorage mpaStorage;
    private GenreDbStorage genreStorage;
    private FilmService filmService;

    private Film testFilm1 = testFilmCreation(null, "Film1", "New Desc", 110L);
    private Film testFilm2 = testFilmCreation(null, "Film2", "New Desc", 110L);
    private Film filmForUpdate = testFilmCreation(1L, "Updated", "New Desc", 110L);
    private Film nonExistFilm = testFilmCreation(999L, "Updated", "New Desc", 110L);

    private User testUser1 = createTestUser(1L);
    private User testUser2 = createTestUser(2L);

    private MPA testMpa = createTestMpa(1L, "PG-13");
    private Genre testGenreDrama = createTestGenre(null, "Драма");
    private Genre testGenreComedy = createTestGenre(null, "Комедия");

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM likes");
        jdbc.update("DELETE FROM films");
        jdbc.update("ALTER TABLE films ALTER COLUMN id RESTART WITH 1");

        mpaStorage = new MPADbStorage(jdbc, new MpaRowMapper());
        genreStorage = new GenreDbStorage(jdbc, new GenreRowMapper());
        filmStorage = new FilmDbStorage(jdbc, new FilmRowMapper(), mpaStorage, genreStorage);
        userStorage = new UserDbStorage(jdbc, new UserRowMapper());

        filmService = new FilmService(filmStorage, userStorage, mpaStorage, genreStorage);

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

    //--------------------------

    @Test
    void createFilmShouldAddFilm() {
        /*не стал выносить filmService.createFilm(testFilm1) в нескольких методах в BeforeEach поскольку зачастую
         * нужен результат + данный метод должен проверять сам факт создания*/
        Film savedFilm = filmService.createFilm(testFilm1);

        assertEquals(1, filmService.getAllFilms().size());
        assertEquals("Film1", filmService.getFilmById(savedFilm.getId()).getName());
    }

    @Test
    void updateFilmShouldUpdateExistingFilm() {
        filmService.createFilm(testFilm1);
        Film result = filmService.updateFilm(filmForUpdate);

        assertEquals("Updated", result.getName());
        assertEquals("New Desc", result.getDescription());
    }

    @Test
    void updateFilmWithWrongIdShouldThrowException() {
        assertThrows(FilmNotFoundException.class, () -> filmService.updateFilm(nonExistFilm));
    }

    @Test
    void addLikeShouldAddUserLike() {
        User user = userStorage.save(testUser1);
        Film film = filmService.createFilm(testFilm1);

        filmService.addLike(film.getId(), user.getId());

        assertTrue(filmStorage.getLikes(film.getId()).contains(1L));
    }

    @Test
    void deleteLikeShouldRemoveUserLike() {
        User user = userStorage.save(testUser1);
        Film film = filmService.createFilm(testFilm1);

        filmService.addLike(film.getId(), user.getId());
        filmService.deleteLike(film.getId(), user.getId());

        assertFalse(filmStorage.getLikes(film.getId()).contains(1L));
    }

    @Test
    void showMostPopularFilmsShouldReturnOrderedByLikes() {
        Film film1 = filmService.createFilm(testFilm1);
        testFilm2.setMpa(testMpa);
        Film film2 = filmService.createFilm(testFilm2);
        User user1 = userStorage.save(testUser1);
        User user2 = userStorage.save(testUser2);

        filmService.addLike(film1.getId(), user1.getId());
        filmService.addLike(film2.getId(), user1.getId());
        filmService.addLike(film2.getId(), user2.getId());

        List<Film> popular = filmService.showMostPopularFilms(2L);

        assertEquals(2, popular.size());
        assertEquals(film2.getId(), popular.get(0).getId());
        assertTrue(filmStorage.getLikes(popular.get(0).getId()).contains(2L));
        assertEquals(film1.getId(), popular.get(1).getId());
    }
}
