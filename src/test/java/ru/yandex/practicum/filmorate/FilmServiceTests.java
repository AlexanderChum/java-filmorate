package ru.yandex.practicum.filmorate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.filmStorage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.filmStorage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.userStorage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.userStorage.UserStorage;

import java.time.LocalDate;
import java.util.*;

class FilmServiceTests {
    private static FilmStorage filmStorage;
    private static UserStorage userStorage;
    private static FilmService filmService;
    private Film testFilm1 = testFilmCreation(null, "Film", "New Desc", 110L);
    private Film testFilm2 = testFilmCreation(null, "Film", "New Desc", 110L);
    private Film filmForUpdate = testFilmCreation(1L, "Updated", "New Desc", 110L);
    private Film nonExistFilm = testFilmCreation(999L, "Updated", "New Desc", 110L);

    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        //filmService = new FilmService(filmStorage, userStorage);
        filmService.createFilm(testFilm1);
        filmService.createFilm(testFilm2);

        User user1 = new User();
        user1.setId(1L);
        user1.setEmail(user1.getId() + "@ya.ru");
        user1.setLogin("user" + user1.getId());
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        userStorage.save(user1);

        User user2 = new User();
        user1.setId(2L);
        user2.setEmail(user2.getId() + "@ya.ru");
        user2.setLogin("user" + user2.getId());
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        userStorage.save(user2);
    }

    private static Film testFilmCreation(Long id, String name, String description, Long duration) {
        Film film = new Film();
        film.setId(id);
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(duration);
        return film;
    }

    @Test
    void createFilmShouldAddFilm() {
        assertNotNull(testFilm1.getId());
        assertEquals(2, filmStorage.getAll().size());
    }

    @Test
    void updateFilmShouldUpdateExistingFilm() {
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
        Film result = filmService.addLike(testFilm1.getId(), 1L);

        assertTrue(result.getLikeSet().contains(1L));
    }

    @Test
    void deleteLikeShouldRemoveUserLike() {
        filmService.addLike(testFilm1.getId(), 1L);
        Film result = filmService.deleteLike(testFilm1.getId(), 1L);

        assertFalse(result.getLikeSet().contains(1L));
    }

    @Test
    void showMostPopularFilmsShouldReturnOrderedByLikes() {
        filmService.addLike(testFilm1.getId(), 1L);
        filmService.addLike(testFilm2.getId(), 1L);
        filmService.addLike(testFilm2.getId(), 1L); //Повторное добавление должно не сработать
        filmService.addLike(testFilm2.getId(), 2L);

        List<Film> popular = filmService.showMostPopularFilms(2L);

        assertEquals(2, popular.size());
        assertEquals(testFilm2.getId(), popular.get(0).getId());
        assertEquals(2, popular.get(0).getLikeSet().size());
        assertEquals(testFilm1.getId(), popular.get(1).getId());
    }
}
