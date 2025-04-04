package ru.yandex.practicum.filmorate.storageTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genreStorage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({GenreDbStorage.class, GenreRowMapper.class})
class GenreDbStorageTest {
    private final JdbcTemplate jdbc;
    private final GenreDbStorage genreStorage;

    private final Genre genreTest = new Genre();
    private final Genre genreTest2 = new Genre();


    @BeforeEach
    void setup() {
        jdbc.update("DELETE FROM genres");
        jdbc.update("ALTER TABLE genres ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    void testAddGenre() {
        genreTest.setName("Комедия");
        Genre addedGenre = genreStorage.addGenre(genreTest);

        assertThat(addedGenre)
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "Комедия");

        Genre storedGenre = genreStorage.getOrCheckGenreById(1L);
        //assertThat(storedGenre).contains(addedGenre);
    }

    @Test
    void testGetOrCheckGenreById() {
        genreTest.setName("Фантастика");
        Genre expected = genreStorage.addGenre(genreTest);

        Genre result = genreStorage.getOrCheckGenreById(expected.getId());

       // assertThat(result)
        //        .contains(expected);
    }

    @Test
    void testGetAllGenres() {
        genreTest.setName("Боевик");
        genreTest2.setName("Мелодрама");
        List<Genre> expected = List.of(
                genreStorage.addGenre(genreTest),
                genreStorage.addGenre(genreTest2)
        );

        List<Genre> result = genreStorage.getAllGenres();

        assertThat(result)
                .isEqualTo(expected);
    }
}