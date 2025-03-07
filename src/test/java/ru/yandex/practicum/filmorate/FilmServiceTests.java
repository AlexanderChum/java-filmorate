package ru.yandex.practicum.filmorate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.*;

class FilmServiceTest {
    private static FilmStorage filmStorage;
    private static UserStorage userStorage;
    private static FilmService filmService;

    @BeforeAll
    static void setUp() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        filmService = new FilmService(filmStorage, userStorage);

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

    private Film testFilmCreation(Long id, String name, String description, Long duration) {
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
        Film created = filmService.createFilm(testFilmCreation(1L, "Film", "Desc", 120L));

        assertNotNull(created.getId());
        assertEquals(1, filmStorage.getAll().size());
    }

    @Test
    void updateFilmShouldUpdateExistingFilm() {
        Film original = filmService.createFilm(testFilmCreation(null, "Original",
                "Desc", 100L));
        Film updated = testFilmCreation(original.getId(), "Updated", "New Desc", 110L);

        Film result = filmService.updateFilm(updated);

        assertEquals("Updated", result.getName());
        assertEquals("New Desc", result.getDescription());
    }

    @Test
    void updateFilmWithWrongIdShouldThrowException() {
        Film film = testFilmCreation(999L, "Film", "Desc", 100L);
        assertThrows(FilmNotFoundException.class, () -> filmService.updateFilm(film));
    }

    @Test
    void addLikeShouldAddUserLike() {
        Film film = filmService.createFilm(testFilmCreation(null, "Film", "Desc", 100L));

        Film result = filmService.addLike(film.getId(), 1L);

        assertTrue(result.getLikeSet().contains(1L));
    }

    @Test
    void deleteLikeShouldRemoveUserLike() {
        Film film = filmService.createFilm(testFilmCreation(null, "Film", "Desc", 100L));
        filmService.addLike(film.getId(), 1L);

        Film result = filmService.deleteLike(film.getId(), 1L);

        assertFalse(result.getLikeSet().contains(1L));
    }

    @Test
    void showMostPopularFilmsShouldReturnOrderedByLikes() {
        Film film1 = filmService.createFilm(testFilmCreation(null, "Film 1", "Desc 1", 100L));
        Film film2 = filmService.createFilm(testFilmCreation(null, "Film 2", "Desc 2", 120L));

        filmService.addLike(film1.getId(), 1L);
        filmService.addLike(film2.getId(), 1L);
        filmService.addLike(film2.getId(), 1L); //Повторное добавление должно не сработать
        filmService.addLike(film2.getId(), 2L);

        List<Film> popular = filmService.showMostPopularFilms(2L);

        assertEquals(2, popular.size());
        assertEquals(film2.getId(), popular.get(0).getId());
        assertEquals(2, popular.get(0).getLikeSet().size());
        assertEquals(film1.getId(), popular.get(1).getId());
    }
}
